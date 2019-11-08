package ru.sberbank.qlik.sense.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApplyPatchesRequest extends BaseRequest<BaseResponse> {
    public ApplyPatchesRequest(int handle, HashMap<String, Object> changes) {
        super("ApplyPatches", BaseResponse.class);
        setHandle(handle);
        HashMap<Object, Object> params = new HashMap<Object, Object>();
        ArrayList<Object> patches = new ArrayList<Object>();
        params.put("qPatches", patches);
        for (Map.Entry<String, Object> stringObjectEntry : changes.entrySet()) {
            HashMap<Object, Object> patch = new HashMap<Object, Object>();
            patch.put("qOp", "replace");
            patch.put("qPath", stringObjectEntry.getKey());
            patch.put("qValue", stringObjectEntry.getValue());
            patches.add(patch);
        }
        params.put("qSoftPatch" , false);
        setParams(params);
    }
}
