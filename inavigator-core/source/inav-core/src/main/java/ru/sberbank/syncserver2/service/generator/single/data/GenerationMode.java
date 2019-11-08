package ru.sberbank.syncserver2.service.generator.single.data;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum GenerationMode {
    ON_CONDITION, PERIODICALLY
}