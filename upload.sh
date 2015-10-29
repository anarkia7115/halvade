#aws s3 cp ./target/halvade-1.0-SNAPSHOT-jar-with-dependencies.jar s3://gcbibucket/halvade/HalvadeWithLibs.jar

./compile.sh

scp ./target/halvade-1.0-SNAPSHOT-jar-with-dependencies.jar gcbi:/home/gcbi/workspace/halvade_run/lib/HalvadeWithLibs.jar

ssh gcbi sh /home/gcbi/workspace/halvade_run/scripts2/run.sh

