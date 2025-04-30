import json
import os
import re
import pymongo
import nltk
from config import MONGO_URI, MONGO_DB_NAME, MONGO_COLLECTION_NAME  

nltk.download("stopwords")

try:
    client = pymongo.MongoClient(
        MONGO_URI,
        serverSelectionTimeoutMS=6000,  
        connectTimeoutMS=6000,          
        socketTimeoutMS=6000            
    )
    db = client[MONGO_DB_NAME]
    collection = db[MONGO_COLLECTION_NAME]
    print("Connected to MongoDB Atlas!")
except Exception as e:
    print(f" Error connecting to MongoDB: {e}")
    exit(1)  


def clean_text(text):
    text = re.sub(r"http\S+", "", text)  
    text = re.sub(r"#\w+", "", text)    
    text = re.sub(r"@\w+", "", text)    
    text = re.sub(r"[^a-zA-Z0-9\s]", "", text)  
    return text.strip()

folder_path = r"C:\Users\parva\OneDrive\Desktop\Dalhousie University Subjects\5408 Data management, warehousing and analytics\GitLab\csci5408_w25_b01026328_parva_mineshbhai_patel\Assignment2\EmailDB\email-db"

if not os.path.exists(folder_path):
    print(f"Error: Folder path '{folder_path}' does not exist!")
    exit(1)

failed_logs = []
cleaned_emails = [] 
processed_emails_set = set()  

for filename in os.listdir(folder_path):
    if filename.endswith(".json"):
        file_path = os.path.join(folder_path, filename)
        try:
            with open(file_path, "r", encoding="utf-8") as file:
                try:
                    file_content = file.read()

                    if file_content.strip().startswith('['): 
                        emails = json.loads(file_content)
                    else:
                        fixed_content = f"[{file_content.replace('}{', '},{')}]"
                        emails = json.loads(fixed_content)

                    if not isinstance(emails, list):
                        raise ValueError("Invalid JSON format: Expected an array of objects")
                except json.JSONDecodeError as e:
                    failed_logs.append(f"JSONDecodeError in {filename}: {e}")
                    continue 

                for email in emails:
                    try:
                        if not all(key in email for key in ["from", "to", "date", "body"]):
                            failed_logs.append(f"Missing fields in {filename}: {email}")
                            continue

                        email["body"] = clean_text(email["body"]) 

                        if all(ord(c) < 128 for c in email["body"]):
                            email_id = (email["from"], email["to"], email["date"])
                            if email_id not in processed_emails_set:
                                processed_emails_set.add(email_id)
                                cleaned_emails.append(email)
                            else:
                                failed_logs.append(f"Duplicate email detected in {filename}: {email}")
                    except Exception as e:
                        failed_logs.append(f" Error processing email in {filename}: {e}")

        except Exception as e:
            failed_logs.append(f"Error opening {filename}: {e}")

if cleaned_emails:
    try:
        collection.insert_many(cleaned_emails)
        print(f"Successfully inserted {len(cleaned_emails)} emails into MongoDB!")
    except Exception as e:
        print(f"Error inserting emails into MongoDB: {e}")

if failed_logs:
    os.makedirs("logs", exist_ok=True)  
    with open("logs/insert_errors.log", "w", encoding="utf-8") as log_file:
        log_file.write("\n".join(failed_logs))
    print(f"{len(failed_logs)} errors logged in 'logs/insert_errors.log'.")

print("Data processing complete.")
