package ru.myproject.cadence.controller;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.myproject.cadence.service.ListService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CadenceController {

    private final ListService listService;

    @GetMapping("showElements")
    public void showElements() {
        listService.letsRock(Arrays.asList("1", "2"));
    }
}
