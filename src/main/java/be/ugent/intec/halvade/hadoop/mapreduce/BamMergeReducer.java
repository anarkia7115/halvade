/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.intec.halvade.hadoop.mapreduce;

import be.ugent.intec.halvade.utils.HalvadeConf;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.seqdoop.hadoop_bam.KeyIgnoringBAMOutputFormat;
import org.seqdoop.hadoop_bam.SAMRecordWritable;
import org.seqdoop.hadoop_bam.util.SAMHeaderReader;

/**
 *
 * @author ddecap
 */
public class BamMergeReducer extends Reducer<LongWritable, SAMRecordWritable, LongWritable, SAMRecordWritable> {
    
    protected SAMFileHeader header;
    protected SAMSequenceDictionary dict;
    KeyIgnoringBAMOutputFormat outpFormat;
    protected String RGID = "GROUP1";    
    protected String RGLB = "LIB1";
    protected String RGPL = "ILLUMINA";
    protected String RGPU = "UNIT1";
    protected String RGSM = "SAMPLE1";
    protected SAMReadGroupRecord bamrg;
    protected boolean inputIsBam = false;
    RecordWriter<LongWritable,SAMRecordWritable> recordWriter;
    boolean reportBest = false;

    @Override
    public void run(Context context) throws IOException, InterruptedException {
        super.run(context); 
        recordWriter.close(context);
    }

    @Override
    protected void reduce(LongWritable key, Iterable<SAMRecordWritable> values, Context context) throws IOException, InterruptedException {
        Iterator<SAMRecordWritable> it = values.iterator();
        while(it.hasNext())
            recordWriter.write(key, it.next());
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        outpFormat = new KeyIgnoringBAMOutputFormat();
        String output = HalvadeConf.getOutDir(context.getConfiguration());
        inputIsBam = HalvadeConf.inputIsBam(context.getConfiguration());
        if(inputIsBam) {
            header = SAMHeaderReader.readSAMHeaderFrom(new Path(HalvadeConf.getHeaderFile(context.getConfiguration())), context.getConfiguration());
        } else {
            getReadGroupData(context.getConfiguration());
            header = new SAMFileHeader();
            header.setSequenceDictionary(dict);
            bamrg = new SAMReadGroupRecord(RGID);
            bamrg.setLibrary(RGLB);
            bamrg.setPlatform(RGPL);
            bamrg.setPlatformUnit(RGPU);
            bamrg.setSample(RGSM);
            header.addReadGroup(bamrg);
        }
        
        outpFormat.setSAMHeader(header);
        recordWriter = outpFormat.getRecordWriter(context, new Path(output + "mergedBam.bam"));
    }
    
        protected void getReadGroupData(Configuration conf) {
        String readGroup = HalvadeConf.getReadGroup(conf);
        String[] elements = readGroup.split(" ");
        for(String ele : elements) {
            String[] val = ele.split(":");
            if(val[0].equalsIgnoreCase("id"))
                RGID = val[1];
            else if(val[0].equalsIgnoreCase("lb"))
                RGLB = val[1];
            else if(val[0].equalsIgnoreCase("pl"))
                RGPL = val[1];
            else if(val[0].equalsIgnoreCase("pu"))
                RGPU = val[1];
            else if(val[0].equalsIgnoreCase("sm"))
                RGSM = val[1];
        }
    }
}
