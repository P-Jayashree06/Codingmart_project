package com.bloodbank.common;
import java.util.List; import java.util.function.Function; import org.springframework.data.domain.Page;
public record PageResponse<T>(List<T> content,int page,int size,long totalElements,int totalPages){
 public static <T> PageResponse<T> from(Page<T> page){return new PageResponse<>(page.getContent(),page.getNumber(),page.getSize(),page.getTotalElements(),page.getTotalPages());}
 public static <S,T> PageResponse<T> from(Page<S> page,Function<? super S,? extends T> mapper){List<T> mapped=page.getContent().stream().<T>map(mapper).toList();return new PageResponse<>(mapped,page.getNumber(),page.getSize(),page.getTotalElements(),page.getTotalPages());}}
