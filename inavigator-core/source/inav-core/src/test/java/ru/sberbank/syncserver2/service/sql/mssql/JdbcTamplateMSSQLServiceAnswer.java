package ru.sberbank.syncserver2.service.sql.mssql;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.sberbank.syncserver2.service.sql.SQLTemplateLoader;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.Dataset;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.ResultSetExtractorImpl;
import ru.sberbank.syncserver2.util.constants.INavConstants;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JdbcTamplateMSSQLServiceAnswer implements Answer, Serializable {

    public static final String SELECT_FIELDS_FROM_TABLES_WHERE_CONDITION_GROUP_BY_GROUP_FIELDS_HAVING_GROUP_CONDITION_ORDER_BY_ORDER_FIELDS = "select fields from tables where condition group by groupFields having groupCondition order by orderFields";

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = invocation.getMethod();
        if (m.getName().equalsIgnoreCase("query")) {
            Object[] arguments = invocation.getArguments();
            if ("msSqlStoredProcedureGenius".equals(arguments[0]) || "select ?".equals(arguments[0])) {
                Dataset ds = new Dataset();
                DatasetRow dsr = new DatasetRow();
                dsr.addValue("Все просто здорово!");
                ds.addRow(dsr);
                DatasetRow dsr1 = new DatasetRow();
                dsr1.addValue("И даже больше чем здорово!");
                ds.addRow(dsr1);
                ResultSetExtractorImpl argument = (ResultSetExtractorImpl) arguments[2];
                argument.getResponse().setDataset(ds);
                argument.getResponse().setResult(DataResponse.Result.OK);
                return argument.getResponse();
            }
            if ("msSqlStoredProcedureGenius1".equals(arguments[0])) {
                Dataset ds = new Dataset();
                DatasetRow dsr = new DatasetRow();
                dsr.addValue("That's one!");
                ds.addRow(dsr);
                DatasetRow dsr1 = new DatasetRow();
                dsr1.addValue("And that's two!");
                ds.addRow(dsr1);
                ResultSetExtractorImpl argument = (ResultSetExtractorImpl) arguments[2];
                argument.getResponse().setDataset(ds);
                argument.getResponse().setResult(DataResponse.Result.OK);
                return argument.getResponse();
            }
            if (INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_SQL_SQLTEMPLATELOADER_LOADALL1.equals(arguments[0])) {
                List<SQLTemplateLoader.Template> localTemplates = new ArrayList<SQLTemplateLoader.Template>();
                SQLTemplateLoader.Template t = new SQLTemplateLoader.Template("tamplateA", "sql A");
                localTemplates.add(t);
                t = new SQLTemplateLoader.Template("msSqlStoredProcedureGeniusTamplate", SELECT_FIELDS_FROM_TABLES_WHERE_CONDITION_GROUP_BY_GROUP_FIELDS_HAVING_GROUP_CONDITION_ORDER_BY_ORDER_FIELDS);
                localTemplates.add(t);
                return localTemplates;
            }
            if (INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_SQL_SQLTEMPLATELOADER_LOADALL2.equals(arguments[0])) {
                List<SQLTemplateLoader.SubstDict> localSubstitutes = new ArrayList<SQLTemplateLoader.SubstDict>();
                SQLTemplateLoader.SubstDict t = new SQLTemplateLoader.SubstDict("substCodeA", "subValueA");
                localSubstitutes.add(t);
                t = new SQLTemplateLoader.SubstDict("substCodeA", "subValueA");
                localSubstitutes.add(t);
                return localSubstitutes;
            }
            if (SELECT_FIELDS_FROM_TABLES_WHERE_CONDITION_GROUP_BY_GROUP_FIELDS_HAVING_GROUP_CONDITION_ORDER_BY_ORDER_FIELDS.equals(arguments[0])) {
                Dataset ds = new Dataset();
                DatasetRow dsr = new DatasetRow();
                dsr.addValue("row number one");
                ds.addRow(dsr);
                DatasetRow dsr1 = new DatasetRow();
                dsr1.addValue("row number two");
                ds.addRow(dsr1);
                ResultSetExtractorImpl argument = (ResultSetExtractorImpl) arguments[2];
                argument.getResponse().setDataset(ds);
                argument.getResponse().setResult(DataResponse.Result.OK);
                return argument.getResponse();
            }
        }
        return null;
    }
}
