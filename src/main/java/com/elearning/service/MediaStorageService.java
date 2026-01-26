package com.elearning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class MediaStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public MediaStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        return uploadFile(file, folder, "image");
    }

    public String uploadVideo(MultipartFile file, String folder) throws IOException {
        String key = generateKey(file, folder);
        
        // Upload original file
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        // Trigger async video processing
        triggerVideoProcessing(key);

        return getPublicUrl(key);
    }

    public String uploadDocument(MultipartFile file, String folder) throws IOException {
        return uploadFile(file, folder, "document");
    }

    private String uploadFile(MultipartFile file, String folder, String type) throws IOException {
        String key = generateKey(file, folder);
        
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .metadata(metadata -> metadata.add("file-type", type))
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        return getPublicUrl(key);
    }

    private String generateKey(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        
        return String.format("%s/%s%s", 
            folder, 
            UUID.randomUUID().toString(), 
            extension);
    }

    private String getPublicUrl(String key) {
        if (cloudFrontDomain != null && !cloudFrontDomain.isEmpty()) {
            return String.format("https://%s/%s", cloudFrontDomain, key);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", 
            bucketName, region, key);
    }

    private void triggerVideoProcessing(String videoKey) {
        // This would trigger an AWS Lambda or Step Function for video processing
        // Including: transcoding, thumbnail generation, subtitle extraction
        // For now, we'll just log it
        System.out.println("Triggering video processing for: " + videoKey);
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
        }
    }

    private String extractKeyFromUrl(String url) {
        // Extract key from S3 or CloudFront URL
        if (url.contains("/")) {
            return url.substring(url.indexOf("/", url.indexOf("//") + 2) + 1);
        }
        return url;
    }

    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
