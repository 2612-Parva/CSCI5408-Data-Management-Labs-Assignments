import csv
import json
from datetime import datetime
import email.utils
from pathlib import Path

def parse_email_date(date_str):
    """Convert email date to Neo4J-compatible ISO 8601 format"""
    try:
        tt = email.utils.parsedate_tz(date_str)
        if tt:
            timestamp = email.utils.mktime_tz(tt)
            dt = datetime.utcfromtimestamp(timestamp)
            return dt.strftime('%Y-%m-%dT%H:%M:%SZ')
    except:
        pass
    return '1970-01-01T00:00:00Z'  

def clean_email_data(input_json, output_csv):
    with open(input_json, 'r', encoding='utf-8') as f:
        emails = json.load(f)
    
    with open(output_csv, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=[
            'from_clean', 'to_clean', 'subject', 'body', 
            'date_iso', 'message_id', 'x_mailer', 
            'ip_address', 'attachments'
        ])
        writer.writeheader()
        
        for email in emails:
            writer.writerow({
                'from_clean': email.get('from', '').split('<')[-1].split('>')[0].strip(),
                'to_clean': email.get('to', '').split('<')[-1].split('>')[0].strip(),
                'subject': email.get('subject', 'No Subject'),
                'body': email.get('body', ''),
                'date_iso': parse_email_date(email.get('date')),
                'message_id': email.get('header', {}).get('message_id', 'missing-id'),
                'x_mailer': email.get('header', {}).get('x_mailer', 'unknown'),
                'ip_address': email.get('header', {}).get('ip_address', '0.0.0.0'),
                'attachments': '|'.join(email.get('attachments', []))
            })

input_path = r'EmailDB\email-db\email-db-00.json'
output_path = r'EmailDB\email-db\emails_final.csv'

clean_email_data(input_path, output_path)
print(f"Successfully processed {input_path} to {output_path}")