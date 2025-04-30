import json

df.to_csv("email_sentiment_clusters.csv", index=False)
df.to_json("email_sentiment_clusters.json", orient='records', lines=True)

centroids_df = pd.DataFrame(centroids, columns=['centroid_polarity', 'centroid_subjectivity'])
centroids_df['cluster'] = centroids_df.index
centroids_df.to_csv("cluster_centroids.csv", index=False)
centroids_df.to_json("cluster_centroids.json", orient='records')

rep_emails_df = pd.DataFrame(closest_emails)
rep_emails_df.to_csv("representative_emails.csv", index=False)
rep_emails_df.to_json("representative_emails.json", orient='records')

print(" All results saved! You can now include these in your report.")