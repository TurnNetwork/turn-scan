package com.turn.browser.verify;

/**
 * Where the error code is defined
 */
public interface Errors {

    String OPEN_ISV = "open.error_";

    ErrorMeta SUCCESS = new ErrorMeta( "0", "success");
    ErrorMeta SYS_ERROR = new ErrorMeta( "-9", "System error");

    /** Call non-existent service request: {0} */
    ErrorMeta NO_API = new ErrorMeta( "1", "Service request that does not exist");
    /** The parameters of the service request ({0}) are illegal */
    ErrorMeta ERROR_PARAM = new ErrorMeta( "2", "Illegal parameter");
    /** The service request ({0}) is missing the application key parameter: {1} */
    ErrorMeta NO_APP_ID = new ErrorMeta( "3", "Missing application key parameter");
    /** The application key parameter {1} of the service request ({0}) is invalid */
    ErrorMeta ERROR_APP_ID = new ErrorMeta( "4", "The application key parameter is invalid");
    /** Service request ({0}) requires signature, missing signature parameters: {1} */
    ErrorMeta NO_SIGN_PARAM = new ErrorMeta( "5", "Missing signature parameter");
    /** The signature of the service request ({0}) is invalid */
    ErrorMeta ERROR_SIGN = new ErrorMeta( "6", "Invalid signature");
    /** Request timed out */
    ErrorMeta TIMEOUT = new ErrorMeta( "7", "Request timeout");
    /** Service request ({0}) business logic error */
    ErrorMeta ERROR_BUSI = new ErrorMeta( "8", "Business logic error");
    /** service is not available */
    ErrorMeta SERVICE_INVALID = new ErrorMeta( "9", "Service is not available");
    /** Request time format error */
    ErrorMeta TIME_INVALID = new ErrorMeta( "10", "The request time format is wrong");
    /** Serialization format does not exist */
    ErrorMeta NO_FORMATTER = new ErrorMeta( "11", "Serialization format does not exist");
    /** contactType is not supported */
    ErrorMeta NO_CONTECT_TYPE_SUPPORT = new ErrorMeta( "12", "contectType is not supported");
    /** json format error */
    ErrorMeta ERROR_JSON_DATA = new ErrorMeta( "13", "json format error");
    /** accessToken error */
    ErrorMeta ERROR_ACCESS_TOKEN = new ErrorMeta( "14", "accessToken error");
    /** accessToken expired */
    ErrorMeta EXPIRED_ACCESS_TOKEN = new ErrorMeta( "15", "accessToken expired");
    /** accessToken not found */
    ErrorMeta UNSET_ACCESS_TOKEN = new ErrorMeta( "16", "accessToken not found");
    /** jwt operation failed */
    ErrorMeta ERROR_OPT_JWT = new ErrorMeta( "17", "jwt operation failed");
    /** jwt error */
    ErrorMeta ERROR_JWT = new ErrorMeta( "18", "jwt error");
    /** Encryption algorithm is not supported */
    ErrorMeta ERROR_ALGORITHM = new ErrorMeta( "19", "Encryption algorithm not supported");
    /** ssl interaction error */
    ErrorMeta ERROR_SSL = new ErrorMeta( "20", "ssl interaction error");
    /** jwt expired */
    ErrorMeta ERROR_JWT_EXPIRED = new ErrorMeta( "21", "jwt expired");
    /** File upload error */
    ErrorMeta ERROR_UPLOAD_FILE = new ErrorMeta( "22", "File upload error");
    /** No access rights */
    ErrorMeta NO_PERMISSION = new ErrorMeta( "23", "No access rights");
    /** New SSL interaction is not supported */
    ErrorMeta NEW_SSL_NOT_SUPPORTED = new ErrorMeta( "24", "New SSL interaction is not supported");

    /** Business parameter error */
    ErrorMeta BUSI_PARAM_ERROR = new ErrorMeta("100", "Business parameter error");

}

