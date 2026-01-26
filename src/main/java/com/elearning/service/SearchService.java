package com.elearning.service;

import com.elearning.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public void indexCourse(Course course) {
        elasticsearchOperations.save(course);
    }

    public void deleteCourseFromIndex(Long courseId) {
        elasticsearchOperations.delete(String.valueOf(courseId), Course.class);
    }

    public Page<Course> searchCourses(String query, Pageable pageable) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(org.elasticsearch.index.query.QueryBuilders
                        .multiMatchQuery(query)
                        .field("title")
                        .field("description")
                        .field("tags")
                        .field("instructor.username")
                        .type(org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.BEST_FIELDS))
                .withPageable(pageable)
                .build();

        SearchHits<Course> searchHits = elasticsearchOperations.search(searchQuery, Course.class);
        
        List<Course> courses = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(courses, pageable, searchHits.getTotalHits());
    }

    public Page<Course> searchCoursesByCategory(String category, Pageable pageable) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(org.elasticsearch.index.query.QueryBuilders
                        .termQuery("category.name", category))
                .withPageable(pageable)
                .build();

        SearchHits<Course> searchHits = elasticsearchOperations.search(searchQuery, Course.class);
        
        List<Course> courses = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(courses, pageable, searchHits.getTotalHits());
    }

    public Page<Course> searchCoursesByDifficulty(String difficulty, Pageable pageable) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(org.elasticsearch.index.query.QueryBuilders
                        .termQuery("difficultyLevel", difficulty))
                .withPageable(pageable)
                .build();

        SearchHits<Course> searchHits = elasticsearchOperations.search(searchQuery, Course.class);
        
        List<Course> courses = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(courses, pageable, searchHits.getTotalHits());
    }

    public Page<Course> advancedSearch(String title, String description, String category, 
                                      String difficulty, Pageable pageable) {
        org.elasticsearch.index.query.BoolQueryBuilder boolQuery = 
                org.elasticsearch.index.query.QueryBuilders.boolQuery();

        if (title != null && !title.isEmpty()) {
            boolQuery.must(org.elasticsearch.index.query.QueryBuilders
                    .matchQuery("title", title));
        }

        if (description != null && !description.isEmpty()) {
            boolQuery.must(org.elasticsearch.index.query.QueryBuilders
                    .matchQuery("description", description));
        }

        if (category != null && !category.isEmpty()) {
            boolQuery.must(org.elasticsearch.index.query.QueryBuilders
                    .termQuery("category.name", category));
        }

        if (difficulty != null && !difficulty.isEmpty()) {
            boolQuery.must(org.elasticsearch.index.query.QueryBuilders
                    .termQuery("difficultyLevel", difficulty));
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<Course> searchHits = elasticsearchOperations.search(searchQuery, Course.class);
        
        List<Course> courses = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(courses, pageable, searchHits.getTotalHits());
    }
}
