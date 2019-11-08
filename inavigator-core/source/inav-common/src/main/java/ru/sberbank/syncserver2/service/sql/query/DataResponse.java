package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.*;

/**
 * @author Sergey Erin
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Response")
public class DataResponse {

    @XmlAttribute(name = "Result")
    private Result result;

    @XmlElement(name = "Metadata")
    private DatasetMetaData metadata;

    @XmlElement(name = "Dataset")
    private Dataset dataset;

    @XmlElement(name = "Error")
    private String error;

    public DataResponse() {
    }

    public DatasetMetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(DatasetMetaData metadata) {
        this.metadata = metadata;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "DataResponse {result=" + result + ", metadata=" + metadata
                + ", dataset=" + dataset + ", error=" + error + "}";
    }

    // расположены в обратном порядке прохождения запроса по соответствующей инфраструктуре (права, БД, proxy, DP)
    // используется для сравнения и выявления наилучшего/наихудшего варианта недоступности канала
    public enum Result {
        OK,
        FAIL_ACCESS,
        FAIL_DB,
        FAIL,
        FAIL_DP,
        FAIL_MAINTENANCE
    }

}
