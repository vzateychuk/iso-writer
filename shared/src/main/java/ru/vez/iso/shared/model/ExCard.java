package ru.vez.iso.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExCard {
    private Long id;
    private ExState state;
    private LocalDateTime start;
    private String desc;

    public String toString() {
        return "ExCard(id=" + this.getId()
                + ", state=" + this.getState()
                + ", start=" + this.getStart()
                + ", desc=" + this.getDesc() + ")";
    }
}
