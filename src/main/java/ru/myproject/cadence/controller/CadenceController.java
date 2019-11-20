package ru.myproject.cadence.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.myproject.cadence.service.ListService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CadenceController {

    private final ListService listService;

    @PostMapping("showElements")
    public void showElements(@RequestBody List<String> strings) {
        listService.letsRock(strings);
    }
}
