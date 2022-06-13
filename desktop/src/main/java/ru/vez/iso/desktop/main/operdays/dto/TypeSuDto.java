package ru.vez.iso.desktop.main.operdays.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Example
 * "typeSu": {
 *   "elementId": "e806dd0f-e563-460f-8d66-1f2350b6e6f1",
 *   "code": "CD",
 *   "elementName": "CD/DVD Диск"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeSuDto {

    private String elementId;
    private String code;
    private String elementName;
}
