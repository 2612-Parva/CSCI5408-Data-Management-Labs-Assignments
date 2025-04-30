import pandas as pd
import matplotlib.pyplot as plt
from textblob import TextBlob
from sklearn.cluster import DBSCAN
from sklearn.preprocessing import StandardScaler
import numpy as np

def get_sentiment(text):
    blob = TextBlob(str(text))
    return blob.sentiment.polarity, blob.sentiment.subjectivity

df[['polarity', 'subjectivity']] = df['body'].apply(lambda x: pd.Series(get_sentiment(x)))

plt.figure(figsize=(12, 8), dpi=150)
plt.scatter(df['polarity'], df['subjectivity'], alpha=0.6, c='skyblue', edgecolors='black')
plt.title('Email Sentiment: Polarity vs Subjectivity')
plt.xlabel('Polarity')
plt.ylabel('Subjectivity')
plt.grid(True)
plt.savefig('sentiment_scatter.png')
plt.show()

X = df[['polarity', 'subjectivity']]
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

dbscan = DBSCAN(eps=0.5, min_samples=5)
df['cluster'] = dbscan.fit_predict(X_scaled)

centroids = df.groupby('cluster')[['polarity', 'subjectivity']].mean()

plt.figure(figsize=(12, 8), dpi=150)
for cluster in df['cluster'].unique():
    cluster_data = df[df['cluster'] == cluster]
    plt.scatter(cluster_data['polarity'], cluster_data['subjectivity'], label=f'Cluster {cluster}', alpha=0.6)

plt.scatter(centroids['polarity'], centroids['subjectivity'], color='red', s=100, label='Centroids', marker='X')
plt.title('DBSCAN Clusters of Email Sentiment')
plt.xlabel('Polarity')
plt.ylabel('Subjectivity')
plt.legend()
plt.grid(True)
plt.savefig('sentiment_clusters.png')
plt.show()

closest_emails = []
for cluster_id, centroid in centroids.iterrows():
    cluster_emails = df[df['cluster'] == cluster_id].copy()
    cluster_emails['distance'] = cluster_emails.apply(
        lambda row: np.sqrt((row['polarity'] - centroid['polarity']) ** 2 + 
                            (row['subjectivity'] - centroid['subjectivity']) ** 2), axis=1)
    closest = cluster_emails.loc[cluster_emails['distance'].idxmin()]
    closest_emails.append(closest)

print("\nClosest Emails to Each Cluster Centroid:\n")
for email in closest_emails:
    print({
        "from": email['from'],
        "to": email['to'],
        "subject": email['subject'],
        "body": email['body'],
        "polarity": email['polarity'],
        "subjectivity": email['subjectivity'],
        "cluster": email['cluster']
    })
    print("-" * 100)