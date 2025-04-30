import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from pymongo import MongoClient
from datetime import datetime
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.util import ngrams
import re
import numpy as np

nltk.download('punkt', download_dir='C:\\nltk_data')
nltk.download('stopwords')
nltk.download('averaged_perceptron_tagger') 
stop_words = set(stopwords.words('english'))


client = MongoClient("mongodb://localhost:27017/")
db = client["EmailDB"]
collection = db["emails"]


emails = list(collection.find({}, {"_id": 0}))  
df = pd.DataFrame(emails)


df['date'] = pd.to_datetime(df['date'], errors='coerce', utc=True)

# Drop rows with invalid date entries
invalid_dates = df['date'].isna().sum()
print(f"Dropping {invalid_dates} rows with invalid date format.")
df.dropna(subset=['date'], inplace=True)


total_emails = len(df)

df['attachment_count'] = df['attachments'].apply(lambda x: len(x) if isinstance(x, list) else 0)
avg_attachments = df['attachment_count'].mean()

emails_per_sender = df['from'].value_counts()

print(f"\n Total Emails: {total_emails}")
print(f" Average Attachments per Email: {avg_attachments:.2f}")
print("\n Emails per Sender:")
print(emails_per_sender)

df['day'] = df['date'].apply(lambda x: x.date())  
daily_counts = df.groupby('day').size()

plt.figure(figsize=(12, 5))
daily_counts.plot()
plt.title('Email Traffic Over Time (Daily)')
plt.xlabel('Date')
plt.ylabel('Number of Emails')
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()

def clean_text(text):
    text = re.sub(r'[^A-Za-z\s]', '', str(text)).lower()
    tokens = word_tokenize(text)
    return [word for word in tokens if word not in stop_words and len(word) > 2]

df['subject_tokens'] = df['subject'].fillna('').apply(clean_text)

all_tokens = [word for tokens in df['subject_tokens'] for word in tokens]
bigrams = list(ngrams(all_tokens, 2))

freq_dist = nltk.FreqDist(all_tokens)
top_10_words = freq_dist.most_common(10)

bigram_freq = nltk.FreqDist(bigrams)
top_10_bigrams = bigram_freq.most_common(10)

print("\nTop 10 Most Frequent Words in Subject:")
for word, freq in top_10_words:
    print(f"{word}: {freq}")

print("\nTop 10 Most Frequent Bigrams (Phrases) in Subject:")
for phrase, freq in top_10_bigrams:
    print(f"{' '.join(phrase)}: {freq}")


df['body_word_count'] = df['body'].fillna('').apply(lambda x: len(word_tokenize(str(x))))
avg_word_count = df['body_word_count'].mean()
longest = df[df['body_word_count'] == df['body_word_count'].max()]
shortest = df[df['body_word_count'] == df['body_word_count'].min()]

print(f"\n Average Word Count in Body: {avg_word_count:.2f}")
print("\n Longest Email Body:")
print(longest[['from', 'to', 'date', 'body']].to_string(index=False))
print("\n Shortest Email Body:")
print(shortest[['from', 'to', 'date', 'body']].to_string(index=False))

plt.figure(figsize=(10, 4))
sns.histplot(df['body_word_count'], bins=30, kde=True)
plt.title('Distribution of Email Body Length (Word Count)')
plt.xlabel('Word Count')
plt.ylabel('Frequency')
plt.tight_layout()
plt.show()


has_attachments = df['attachment_count'] > 0
prop_with_attachments = has_attachments.mean()
most_common_attachment_count = df['attachment_count'].value_counts().idxmax()
attachments_by_sender = df.groupby('from')['attachment_count'].sum().sort_values(ascending=False)

print(f"\n Proportion of Emails with Attachments: {prop_with_attachments:.2f}")
print(f" Most Common Attachment Count: {most_common_attachment_count}")
print("\n Top Senders by Total Attachments:")
print(attachments_by_sender.head())

plt.figure(figsize=(10, 5))
attachments_by_sender.head(10).plot(kind='bar', color='gold')
plt.title('Top 10 Senders by Number of Attachments')
plt.ylabel('Attachment Count')
plt.xlabel('Sender')
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()