package com.mcon152.recipeshare.repository;

import com.mcon152.recipeshare.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
//persistence pattern / data access layer
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // repository methods (default CRUD provided by JpaRepository)
}

