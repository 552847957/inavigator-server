package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NxValuePoint {
    public String qText;
    public Double qNum;
}
