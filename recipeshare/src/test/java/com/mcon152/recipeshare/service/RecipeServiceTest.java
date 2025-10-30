package com.mcon152.recipeshare.service;

import com.mcon152.recipeshare.Recipe;
import com.mcon152.recipeshare.repository.RecipeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Assignment: Implement all TODOs using Mockito features covered in class:
 *  - @Mock, @InjectMocks, @Captor, @ExtendWith(MockitoExtension.class)
 *  - Stubbing: thenReturn / thenAnswer / thenThrow
 *  - Verifications: verify(...), times/never/atLeast..., verifyNoMoreInteractions
 *  - InOrder (where meaningful)
 *  - Void stubbing: doNothing / doThrow (use deleteById for this)
 *  - Matchers: any(), eq(), argThat()
 *  - ArgumentCaptor
 *  - (Optional) Spy demo if you introduce a small helper in tests
 * NOTE: This is a pure unit test. Do NOT start a Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService (Mockito) — Assignment Skeleton")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService; // CUT implements RecipeService

    @Captor
    private ArgumentCaptor<Recipe> recipeCaptor;

    // --- Helpers for sample data ---

    private Recipe newRecipeNoId() {
        return new Recipe(
                null,
                "Chocolate Cake",
                "Moist chocolate cake",
                "flour, eggs, cocoa",
                "mix, bake",
                8
        );
    }

    private Recipe savedRecipe(long id) {
        return new Recipe(
                id,
                "Chocolate Cake",
                "Moist chocolate cake",
                "flour, eggs, cocoa",
                "mix, bake",
                8
        );
    }

    // ------------------ addRecipe ------------------

    @Nested
    @DisplayName("addRecipe(Recipe)")
    class AddRecipe {

        @Test
        @DisplayName("returns saved entity (thenReturn) and calls repository.save once")
        void returnsSaved_andSavesOnce() {
            // 1) when(recipeRepository.save(...)).thenReturn(savedRecipe(1L))
            // 2) call recipeService.addRecipe(newRecipeNoId())
            // 3) assert non-null id and fields
            // 4) verify(recipeRepository).save(any(Recipe.class)); verifyNoMoreInteractions(recipeRepository)

            //See code below as an example answer

            Recipe input = newRecipeNoId();
            Recipe saved = savedRecipe(1L);

            when(recipeRepository.save(any(Recipe.class))).thenReturn(saved);

            Recipe out = recipeService.addRecipe(input);
            assertEquals(1L, out.getId());
            assertEquals(saved, out);

            verify(recipeRepository).save(any(Recipe.class));
            verifyNoMoreInteractions(recipeRepository);
        }

        @Test
        @DisplayName("assigns ID dynamically (thenAnswer) and captures argument")
        void assignsId_thenAnswer_andCaptures() {
            // 1) Use thenAnswer to return a new Recipe with id=1L, copying fields from arg
            // 2) capture the arg with ArgumentCaptor and assert title, id==null pre-save

            //See code below as an example answer

            when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
                Recipe r = inv.getArgument(0);
                return new Recipe(1L, r.getTitle(), r.getDescription(),
                        r.getIngredients(), r.getInstructions(), r.getServings());
            });

            Recipe out = recipeService.addRecipe(newRecipeNoId());
            assertEquals(1L, out.getId());

            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe sent = recipeCaptor.getValue();
            assertNull(sent.getId()); // before persistence
            assertEquals("Chocolate Cake", sent.getTitle());
        }

        @Test
        @DisplayName("propagates repository failure (thenThrow)")
        void propagatesRepositoryFailure() {
            // when(recipeRepository.save(any())).thenThrow(new IllegalStateException("DB down"))
            // assertThrows on recipeService.addRecipe(...)
            // (checking that exceptions come through)

            when(recipeRepository.save(any(Recipe.class))).thenThrow(new IllegalStateException("DB down"));

            assertThrows(IllegalStateException.class, () -> recipeService.addRecipe(savedRecipe(123)));

            verify(recipeRepository).save(any(Recipe.class));
            verifyNoMoreInteractions(recipeRepository);
        }
    }

    // ------------------ getAllRecipes ------------------

    @Nested
    @DisplayName("getAllRecipes()")
    class GetAllRecipes {

        @Test
        @DisplayName("returns list from repository")
        void returnsList() {
            // when(recipeRepository.findAll()).thenReturn(List.of(...))
            // assert same size/content; verify(findAll)

            Recipe saved =  savedRecipe(123);
            when(recipeRepository.findAll()).thenReturn(List.of(saved));

            assertEquals(List.of(saved), recipeService.getAllRecipes());

            verify(recipeRepository).findAll();
            verifyNoMoreInteractions(recipeRepository);

        }
    }

    // ------------------ getRecipeById ------------------

    @Nested
    @DisplayName("getRecipeById(long)")
    class GetById {

        @Test
        @DisplayName("returns Optional.present when found")
        void present() {
            // stub findById(1L)->Optional.of(savedRecipe(1L)), assert present
            when(recipeRepository.findById(1L)).thenReturn(Optional.of(savedRecipe(1L)));

            assertTrue(recipeService.getRecipeById(1L).isPresent());

            verify(recipeRepository).findById(1L);
            verifyNoMoreInteractions(recipeRepository);
        }

        @Test
        @DisplayName("returns Optional.empty when missing")
        void empty() {
            // stub Optional.empty, assert empty
            when(recipeRepository.findById(2L)).thenReturn(Optional.empty());

            assertTrue(recipeService.getRecipeById(2L).isEmpty());

            verify(recipeRepository).findById(2L);
            verifyNoMoreInteractions(recipeRepository);
        }

    }

    // ------------------ deleteRecipe ------------------

    @Nested
    @DisplayName("deleteRecipe(long)")
    class DeleteRecipe {

        @Test
        @DisplayName("returns true when entity existed")
        void returnsTrue_whenExists() {
            // when(recipeRepository.existsById(id)).thenReturn(true)
            // doNothing().when(recipeRepository).deleteById(id)
            // assert true; verify order: existsById -> deleteById

            when(recipeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(recipeRepository).deleteById(1L);

            assertTrue(recipeService.deleteRecipe(1L));

            InOrder inOrder = Mockito.inOrder(recipeRepository);
            inOrder.verify(recipeRepository).existsById(1L);
            inOrder.verify(recipeRepository).deleteById(1L);
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("returns false when missing (never deletes)")
        void returnsFalse_whenMissing() {
            // existsById -> false; assert false; verify deleteById never called

            when(recipeRepository.existsById(1L)).thenReturn(false);

            assertFalse(recipeService.deleteRecipe(1L));

            verify(recipeRepository).existsById(1L);
            verifyNoMoreInteractions(recipeRepository);

        }

        @Test
        @DisplayName("propagates delete error (doThrow)")
        void propagatesDeleteError() {
            // existsById -> true; doThrow(...) on deleteById; assertThrows

            when(recipeRepository.existsById(1L)).thenReturn(true);
            doThrow(new EmptyResultDataAccessException(1)).when(recipeRepository).deleteById(1L);

            assertThrows(EmptyResultDataAccessException.class, () -> recipeService.deleteRecipe(1L));

            InOrder inOrder = Mockito.inOrder(recipeRepository);
            inOrder.verify(recipeRepository).existsById(1L);
            inOrder.verify(recipeRepository).deleteById(1L);
            inOrder.verifyNoMoreInteractions();
        }
    }

    // ------------------ updateRecipe ------------------

    @Nested
    @DisplayName("updateRecipe(long, Recipe)")
    class UpdateRecipe {

        @Test
        @DisplayName("returns updated entity when exists")
        void returnsUpdated_whenExists() {
            // findById -> present(existing)
            // save(...) -> updatedSaved
            // assert Optional.present & fields updated
            // capture arg and assert values

            Recipe existing = savedRecipe(1L);
            Recipe updated = savedRecipe(1L);
            updated.setTitle("Brownies");

            when(recipeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any(Recipe.class))).thenReturn(updated);

            Optional<Recipe> updatedRecipe = recipeService.updateRecipe(existing.getId(), updated);
            assertTrue(updatedRecipe.isPresent());
            assertEquals(1L, updatedRecipe.get().getId());
            assertEquals("Brownies", updatedRecipe.get().getTitle());

            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe sent = recipeCaptor.getValue();
            assertEquals("Brownies", sent.getTitle());

            verifyNoMoreInteractions(recipeRepository);
         }

        @Test
        @DisplayName("returns empty when entity missing")
        void returnsEmpty_whenMissing() {
            // findById -> empty; assert Optional.empty; verify save never called
            when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

            assert(recipeService.updateRecipe(1L, newRecipeNoId()).isEmpty());

            verify(recipeRepository).findById(1L);
            verifyNoMoreInteractions(recipeRepository);
        }
    }

    // ------------------ patchRecipe ------------------

    @Nested
    @DisplayName("patchRecipe(long, Recipe)")
    class PatchRecipe {

        @Test
        @DisplayName("applies only non-null fields (argThat)")
        void appliesNonNullFields_only() {
            // findById -> present(existing)
            // provide partial with only title set
            // repository.save returns the modified entity (use thenAnswer echo)
            // verify save(argThat(...)) to ensure unchanged fields remain as-is

            Recipe existing = savedRecipe(1L);
            Recipe partial =  new Recipe();
            partial.setTitle("Brownies");

            when(recipeRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

            recipeService.patchRecipe(existing.getId(), partial);

            verify(recipeRepository).findById(1L);
            verify(recipeRepository).save(argThat(recipe ->
                    recipe.getId().equals(1L) &&
                    recipe.getTitle().equals("Brownies") &&
                    recipe.getDescription().equals("Moist chocolate cake") &&
                    recipe.getServings().equals(8)
            ));
            verifyNoMoreInteractions(recipeRepository);

        }

        @Test
        @DisplayName("returns empty when entity missing")
        void returnsEmpty_whenMissing() {
            // findById -> empty; assert Optional.empty; verify save never called
            when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

            assert(recipeService.patchRecipe(1L, newRecipeNoId()).isEmpty());

            verify(recipeRepository).findById(1L);
            verifyNoMoreInteractions(recipeRepository);
         }
    }

    // ------------------ extra practice ------------------

    @Nested
    @DisplayName("Advanced stubbing & verification")
    class Advanced {

        @Test
        @DisplayName("consecutive stubs on existsById (true, false)")
        void consecutiveStubs_existsById() {
            // when(existsById(1L)).thenReturn(true, false); verify two calls and no more
            when(recipeRepository.existsById(1L)).thenReturn(true, false);

            assertTrue(recipeRepository.existsById(1L));
            assertFalse(recipeRepository.existsById(1L));

            verify(recipeRepository, times(2)).existsById(1L);
            verifyNoMoreInteractions(recipeRepository);
         }
    }
}
