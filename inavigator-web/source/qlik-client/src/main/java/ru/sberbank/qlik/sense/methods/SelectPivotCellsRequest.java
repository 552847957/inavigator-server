package ru.sberbank.qlik.sense.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//{"method":"SelectPivotCells","handle":8,"params":["/qHyperCubeDef",[{"qType":"L","qCol":0,"qRow":1}]],"delta":false,"jsonrpc":"2.0","id":222}
//{"method":"SelectPivotCells","handle":8,"params":["/qHyperCubeDef",[{"qType":"T","qCol":1,"qRow":0}]],"delta":false,"jsonrpc":"2.0","id":253}
public class SelectPivotCellsRequest extends BaseRequest<BaseResponse> {
    public SelectPivotCellsRequest(int handle, List<Integer> rows, List<Integer> cols) {
        super("SelectPivotCells", BaseResponse.class);
        setHandle(handle);
        ArrayList<Object> query = new ArrayList<Object>();
        if(rows != null) {
            HashSet<Integer> rowsSet = new HashSet<Integer>(rows);
            for (Integer row : rowsSet) {
                HashMap<String, Object> queryParams = new HashMap<String, Object>();
                queryParams.put("qType", "L");
                queryParams.put("qCol", 0);
                queryParams.put("qRow", row);
                query.add(queryParams);
            }
        }

        if(cols != null) {
            HashSet<Integer> colsSet = new HashSet<Integer>(cols);
            for (Integer col : colsSet) {
                HashMap<String, Object> queryParams = new HashMap<String, Object>();
                queryParams.put("qType", "T");
                queryParams.put("qCol", col);
                queryParams.put("qRow", 0);
                query.add(queryParams);
            }
        }
        Object[] params = {"/qHyperCubeDef", query};
        setParams(params);
    }
}
