package ru.vez.abdd;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
class AbddRestController {

    /*
    * provides ЕХ-card for period.
    * */
    @GetMapping(value = "/cards")
    public List<EXcard> GetStorageUnitList() {
        return getListOf(10);
    }

    private List<EXcard> getListOf(long size) {

        return ThreadLocalRandom.current().ints(size)
                .mapToObj(i -> new EXcard(Integer.valueOf(i).toString(), LocalDateTime.now(), "card " + i))
                .collect(Collectors.toList());
    }

}