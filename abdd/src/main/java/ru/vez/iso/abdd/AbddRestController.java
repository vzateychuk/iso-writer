package ru.vez.iso.abdd;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vez.iso.shared.model.ExCard;
import ru.vez.iso.shared.model.ExState;

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
    public List<ExCard> GetStorageUnitList() {
        return getListOf(10);
    }

    private List<ExCard> getListOf(long size) {

        return ThreadLocalRandom.current().ints(size)
                .mapToObj(i -> new ExCard(Integer.valueOf(i).toString(), ExState.READY, LocalDateTime.now(), "card " + i))
                .collect(Collectors.toList());
    }

}