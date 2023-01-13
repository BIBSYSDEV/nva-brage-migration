# BRAGE migration handle identifier downloader

## Documentation

Given script creates a handles.csv file and writes all handles and corresponding NVA publication identifiers.
Handle-Identifier pairs will be written to file in following format:

some/handle,identifier
some/handle,identifier

## Run the script

To run the script you have to run handleDownloader.py and send in subpath from s3 bucket as parameter 
which will be used to get all the files from this s3 location. 

Script runs properly with Python 3.10.6

Example script:
```shell
python3 handleDownloader.py 2022-12-12:07/
```





