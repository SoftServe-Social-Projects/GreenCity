package greencity.controller;

import greencity.dto.todolistitem.ToDoListItemRequestDto;
import greencity.service.ToDoListItemService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.validation.Validator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ToDoListItemControllerTest {
    private static final String toDoListItemLink = "/habits/to-do-list-items";
    private MockMvc mockMvc;
    @InjectMocks
    private ToDoListItemController toDoListItemController;
    @Mock
    private ToDoListItemService toDoListItemService;
    @Mock
    private Validator mockValidator;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(toDoListItemController)
            .setValidator(mockValidator)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }
}
