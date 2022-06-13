package ru.vez.iso.desktop.main.noop;

import org.apache.http.client.methods.HttpPost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.UtilsHelper;

/**
 * HttpClient wrapper NOOP implementation
 * Successful server response expected:
 * {
 *     "objects": [
 *       {
 *         "objectId": "18f17937-bd4e-4b69-9a12-10ad447e6fbe",
 *         "objectName": "Операционный день АБДД",
 *         "versionId": null,
 *         "creator": "admin",
 *         "modifier": "admin",
 *         "creationDate": "2022-05-27T13:57:15.899726",
 *         "modifyDate": "2022-05-27T13:57:15.899903",
 *         "objectState": null,
 *         "acl": {},
 *         "deleted": false,
 *         "typeName": "OperatingDayAbdd",
 *         "hasFile": false,
 *         "timeStamp": null,
 *         "uploadingOperationId": null,
 *         "numberSu": "2022-05-27",
 *         "operatingDayDate": "2022-05-27",
 *         "typeSu": {
 *           "elementId": "e806dd0f-e563-460f-8d66-1f2350b6e6f1",
 *           "code": "CD",
 *           "elementName": "CD/DVD Диск"
 *         },
 *         "operatingDayStatus": "NEW"
 *       }
 * 	],
 *     "count": 1
 * }
 * */
public class HttpClientOperationDaysNoopImpl implements HttpClientWrap {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String postDataRequest(HttpPost httpPost) {

        logger.debug(httpPost.toString());

        UtilsHelper.makeDelaySec(1);

        return UtilsHelper.readJsonFromFile("noop/data/operationDays.json");
    }
}
