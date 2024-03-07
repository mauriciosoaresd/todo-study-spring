package com.exercise.todotest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exercise.todotest.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Integer>{
}
