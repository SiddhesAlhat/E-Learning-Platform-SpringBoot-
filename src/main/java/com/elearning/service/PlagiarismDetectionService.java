package com.elearning.service;

import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlagiarismDetectionService {

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    /**
     * Calculate plagiarism score between two texts using cosine similarity
     * Returns a score between 0 (completely different) and 100 (identical)
     */
    public double calculatePlagiarismScore(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        // Clean and normalize texts
        text1 = normalizeText(text1);
        text2 = normalizeText(text2);

        // Calculate cosine similarity
        double cosineSim = calculateCosineSimilarity(text1, text2);
        
        // Calculate Levenshtein similarity for additional accuracy
        double levenSim = calculateLevenshteinSimilarity(text1, text2);
        
        // Weighted average (cosine is more reliable for longer texts)
        double finalScore = (cosineSim * 0.7) + (levenSim * 0.3);
        
        return finalScore * 100; // Convert to percentage
    }

    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    private double calculateCosineSimilarity(String text1, String text2) {
        Map<CharSequence, Integer> vector1 = createWordFrequencyMap(text1);
        Map<CharSequence, Integer> vector2 = createWordFrequencyMap(text2);

        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        return cosineSimilarity.cosineSimilarity(vector1, vector2);
    }

    private Map<CharSequence, Integer> createWordFrequencyMap(String text) {
        Map<CharSequence, Integer> map = new HashMap<>();
        String[] words = text.split("\\s+");
        
        for (String word : words) {
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
        
        return map;
    }

    private double calculateLevenshteinSimilarity(String text1, String text2) {
        int maxLength = Math.max(text1.length(), text2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = levenshteinDistance.apply(text1, text2);
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Check for code plagiarism using N-gram analysis
     */
    public double calculateCodePlagiarismScore(String code1, String code2) {
        if (code1 == null || code2 == null) return 0.0;

        // Remove comments and normalize code
        code1 = removeComments(code1);
        code2 = removeComments(code2);

        // Use 3-grams for code comparison
        Set<String> ngrams1 = generateNGrams(code1, 3);
        Set<String> ngrams2 = generateNGrams(code2, 3);

        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(ngrams1);
        intersection.retainAll(ngrams2);

        Set<String> union = new HashSet<>(ngrams1);
        union.addAll(ngrams2);

        if (union.isEmpty()) return 0.0;

        return ((double) intersection.size() / union.size()) * 100;
    }

    private String removeComments(String code) {
        // Remove single-line comments
        code = code.replaceAll("//.*", "");
        // Remove multi-line comments
        code = code.replaceAll("/\\*.*?\\*/", "");
        return code.replaceAll("\\s+", " ").trim();
    }

    private Set<String> generateNGrams(String text, int n) {
        Set<String> ngrams = new HashSet<>();
        String[] words = text.split("\\s+");
        
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                ngram.append(words[i + j]).append(" ");
            }
            ngrams.add(ngram.toString().trim());
        }
        
        return ngrams;
    }
}
