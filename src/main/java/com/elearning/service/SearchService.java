package com.elearning.service;

import com.elearning.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

import java.util.ArrayList;
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
                Query multiMatchQuery = MultiMatchQuery.of(m -> m
                                .query(query)
                                .fields("title", "description", "tags", "instructor.username"))._toQuery();

                NativeQuery searchQuery = NativeQuery.builder()
                                .withQuery(multiMatchQuery)
                                .withPageable(pageable)
                                .build();

                SearchHits<Course> searchHits = elasticsearchOperations.search(searchQuery, Course.class);

                List<Course> courses = searchHits.stream()
                                .map(SearchHit::getContent)
                                .collect(Collectors.toList());

                return new PageImpl<>(courses, pageable, searchHits.getTotalHits());
        }

        public Page<Course> searchCoursesByCategory(String category, Pageable pageable) {
                Query termQuery = TermQuery.of(t -> t
                                .field("category.name")
                                .value(category))._toQuery();

                NativeQuery searchQuery = NativeQuery.builder()
                                .withQuery(termQuery)
                                .withPageable(pageable)
                                .build();

                SearchHits<Course> searchHits = elasticsearchOperations.search(searchQuery, Course.class);

                List<Course> courses = searchHits.stream()
                                .map(SearchHit::getContent)
                                .collect(Collectors.toList());

                return new PageImpl<>(courses, pageable, searchHits.getTotalHits());
        }

        public Page<Course> searchCoursesByDifficulty(String difficulty, Pageable pageable) {
                Query termQuery = TermQuery.of(t -> t
                                .field("difficultyLevel")
                                .value(difficulty))._toQuery();

                NativeQuery searchQuery = NativeQuery.builder()
                                .withQuery(termQuery)
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
                List<Query> mustQueries = new ArrayList<>();

                if (title != null && !title.isEmpty()) {
                        mustQueries.add(MatchQuery.of(m -> m
                                        .field("title")
                                        .query(title))._toQuery());
                }

                if (description != null && !description.isEmpty()) {
                        mustQueries.add(MatchQuery.of(m -> m
                                        .field("description")
                                        .query(description))._toQuery());
                }

                if (category != null && !category.isEmpty()) {
                        mustQueries.add(TermQuery.of(t -> t
                                        .field("category.name")
                                        .value(category))._toQuery());
                }

                if (difficulty != null && !difficulty.isEmpty()) {
                        mustQueries.add(TermQuery.of(t -> t
                                        .field("difficultyLevel")
                                        .value(difficulty))._toQuery());
                }

                Query boolQuery = BoolQuery.of(b -> b
                                .must(mustQueries))._toQuery();

                NativeQuery searchQuery = NativeQuery.builder()
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
