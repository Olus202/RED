import commonmark
import argparse
import os


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', dest='file', help='.md file to change to CommonMark', required=True)
    args = parser.parse_args()

    md_file = args.file
    
    print("open file")
    with open(md_file, 'r+b') as file:
        md_content = file.read()
        #print(file.read())
        if md_content != "":
            try:
                md_content_conv = commonmark.commonmark(md_content)
                print(md_content_conv)
                file.seek(0, os.SEEK_CUR)
                file.write(md_content_conv)
            except IOError as e:
			   print(e)
				
    print("done")