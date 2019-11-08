package ru.sberbank.qlik.sense.methods;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.sberbank.qlik.sense.objects.QHyperCubeDef;
import ru.sberbank.qlik.sense.objects.QInfo;


public class GetEffectivePropertiesResponse extends BaseResponse<GetEffectivePropertiesResponse.Result> {

    public static class Result {
        public QProp qProp;

        public QProp getqProp() {
            return qProp;
        }

        public void setqProp(QProp qProp) {
            this.qProp = qProp;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QProp {
        public QInfo qInfo;
        public QHyperCubeDef qHyperCubeDef;

        public QInfo getqInfo() {
            return qInfo;
        }

        public void setqInfo(QInfo qInfo) {
            this.qInfo = qInfo;
        }

        public QHyperCubeDef getqHyperCubeDef() {
            return qHyperCubeDef;
        }

        public void setqHyperCubeDef(QHyperCubeDef qHyperCubeDef) {
            this.qHyperCubeDef = qHyperCubeDef;
        }
    }
}
