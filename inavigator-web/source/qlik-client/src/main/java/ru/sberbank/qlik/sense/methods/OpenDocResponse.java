package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.QType;


public class OpenDocResponse extends BaseResponse<OpenDocResponse.Result> {

    public static class Result  {
        public QReturn qReturn;

        public QReturn getqReturn() {
            return qReturn;
        }

        public void setqReturn(QReturn qReturn) {
            this.qReturn = qReturn;
        }
    }

    public static class QReturn {
        public QType qType;
        public Integer qHandle;
        public String qGenericId;

        public QType getqType() {
            return qType;
        }

        public void setqType(QType qType) {
            this.qType = qType;
        }

        public Integer getqHandle() {
            return qHandle;
        }

        public void setqHandle(Integer qHandle) {
            this.qHandle = qHandle;
        }

        public String getqGenericId() {
            return qGenericId;
        }

        public void setqGenericId(String qGenericId) {
            this.qGenericId = qGenericId;
        }
    }
}


