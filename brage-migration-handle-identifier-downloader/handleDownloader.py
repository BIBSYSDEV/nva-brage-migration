import sys
import boto3

s3 = boto3.resource('s3')
bucket = s3.Bucket('brage-migration-reports-750639270376')
text_file = open("handles.csv", "w")
time = sys.argv[-1]


def write_to_file():
  for object in bucket.objects.filter(Prefix="HANDLE_REPORTS/" + time):
    handle = "/".join(
      object.get()["Body"].read().decode("utf-8").split("/")[-2:])
    identifier = object.key.split("/")[-1]
    text_file.write(handle + "," + identifier + "\n")


if __name__ == '__main__':
  write_to_file()

