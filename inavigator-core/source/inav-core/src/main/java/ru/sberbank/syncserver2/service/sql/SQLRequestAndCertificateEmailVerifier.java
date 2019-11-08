package ru.sberbank.syncserver2.service.sql;

import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.FormatHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by sbt-Shakhov-IN on 23.06.2017.
 */
public class SQLRequestAndCertificateEmailVerifier extends BackgroundService implements PublicService, SQLService {
    private SQLService originalService;
    private String skipEmailVerification = "false";
    private String skipCertificateEmailCheckingString = null;
    private volatile boolean skipCertificateEmailChecking = true;
    private String skipEmailVerificationIps;

    private String splitByString = "<paramname=\"email\"value=\"";
    private String closeByString = "\"";

    private SQLService verifyStrategy = new SQLService() {
        @Override
        public DataResponse request(OnlineRequest request) {
            String email = getEmailFromArg(getFirstArgumentOrNull(request));
            if (email != null) {
                int skipVerification = skipEmailVerificationIps == null ? -1 : skipEmailVerificationIps.indexOf(request.getUserIpAddress() == null ? "" : request.getUserIpAddress());
                if (skipVerification >= 0) {
                    tagLogger.log(FormatHelper.stringConcatenator("Проверку на email не делаем, ", request.getUserIpAddress(), " имеется в ", skipEmailVerificationIps));
                    return originalService.request(request);
                } else {
                    tagLogger.log(FormatHelper.stringConcatenator("Делаем проверку на email, ", request.getUserIpAddress(), " нету в ", skipEmailVerificationIps));
                    if (email.equalsIgnoreCase(request.getUserEmail())) {
                        return originalService.request(request);
                    }
                }
            } else {
                if (skipCertificateEmailChecking || request.getUserEmail() != null) {
                    return originalService.request(request);
                }
            }

            DataResponse response = new DataResponse();
            response.setResult(DataResponse.Result.FAIL_ACCESS);
            response.setError("Запрещено выполнение запроса для пользователя " + email + " с использованием сертификата " + request.getUserEmail());
            return response;
        }
    };

    private SQLService doNotVerifyStrategy = new SQLService() {
        @Override
        public DataResponse request(OnlineRequest request) {
            if (skipCertificateEmailChecking || request.getUserEmail() != null) {
                return originalService.request(request);
            } else {
                DataResponse response = new DataResponse();
                response.setResult(DataResponse.Result.FAIL_ACCESS);
                response.setError("Запрещено выполнение запроса без использования личного сертификата");
                return response;
            }
        }
    };

    private volatile SQLService strategy = doNotVerifyStrategy;

    @Override
    protected void doStart() {
        super.doStart();
        if (skipCertificateEmailCheckingString != null) {
            skipCertificateEmailChecking = Boolean.parseBoolean(skipCertificateEmailCheckingString);
        }
        if (!Boolean.parseBoolean(skipEmailVerification)) {
            strategy = verifyStrategy;
            tagLogger.log("Включена проверка email в запросе");
        } else {
            strategy = doNotVerifyStrategy;
            tagLogger.log("Проверка email в запросе выключена");
        }
    }

    @Override
    public DataResponse request(OnlineRequest request) {
        return strategy.request(request);
    }

    private String getFirstArgumentOrNull(OnlineRequest request) {
        if (request.getArguments() == null ||
                request.getArguments().getArgument() == null ||
                request.getArguments().getArgument().isEmpty())
            return null;

        return request.getArguments().getArgument().get(0).getValue();
    }

    private String getEmailFromArg(String arg) {
        if (arg == null)
            return null;
        arg = arg.replaceAll(" ", "").toLowerCase();
        int index = arg.indexOf(splitByString);
        if (index != -1) {
            String temp = arg.substring(index + splitByString.length());
            index = temp.indexOf(closeByString);
            return index == -1 ? null : temp.substring(0, index);
        }
        return null;
    }

    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doStop() {
        //nothing to do
    }

    public String getSkipEmailVerification() {
        return skipEmailVerification;
    }

    public void setSkipEmailVerification(String skipCheckOutEmail) {
        this.skipEmailVerification = skipCheckOutEmail;
    }

    public void setCloseByString(String closeByString) {
        this.closeByString = closeByString;
    }

    public void setSplitByString(String splitByString) {
        this.splitByString = splitByString;
    }

    public void setSkipCertificateEmailChecking(String skipCertificateEmailCheckingString) {
        this.skipCertificateEmailCheckingString = skipCertificateEmailCheckingString;
    }

    public void setOriginalService(SQLService originalService) {
        this.originalService = originalService;
    }

    public String getSkipEmailVerificationIps() {
        return skipEmailVerificationIps;
    }

    public void setSkipEmailVerificationIps(String skipEmailVerificationIps) {
        this.skipEmailVerificationIps = skipEmailVerificationIps;
    }

}
