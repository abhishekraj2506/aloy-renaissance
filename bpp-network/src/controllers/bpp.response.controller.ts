import * as AmqbLib from "amqplib";
import { Exception, ExceptionType } from "../models/exception.model";
import { BecknErrorType } from "../schemas/becknError.schema";
import { BecknResponse } from "../schemas/becknResponse.schema";
import {
  NetworkPaticipantType,
  SubscriberDetail
} from "../schemas/subscriberDetails.schema";
import { ActionUtils } from "../utils/actions.utils";
import { createAuthHeaderConfig } from "../utils/auth.utils";
import { callNetwork } from "../utils/becknRequester.utils";
import { RequestCache } from "../utils/cache/request.cache.utils";
import { errorCallback } from "../utils/callback.utils";
import logger from "../utils/logger.utils";

export const sellerServiceMessageHandler = async (
  msg: AmqbLib.ConsumeMessage | null
) => {
  try {
    const responseBody = JSON.parse(msg?.content.toString()!);
    const context = JSON.parse(JSON.stringify(responseBody.context));
    const message_id = responseBody.context.message_id;
    const requestAction = ActionUtils.getCorrespondingRequestAction(
      responseBody.context.action
    );
    const action = context.action;
    const bap_uri = responseBody.context.bap_uri;
    logger.info(`Received message ${responseBody} from seller service`)
    const requestCache = await RequestCache.getInstance().check(
      message_id,
      requestAction
    );
    if (!requestCache) {
      errorCallback(context, {
        // TODO: change this error code.
        code: 651641,
        type: BecknErrorType.coreError,
        message: "Request timed out"
      });
      return;
    }

    const axios_config = await createAuthHeaderConfig(responseBody);

    let response: BecknResponse | null = null;
    if (requestCache.sender.type == NetworkPaticipantType.BG) {
      const subscribers = [requestCache.sender];

      response = await callNetwork(
        subscribers,
        responseBody,
        axios_config,
        action
      );
    } else {
      const subscribers: Array<SubscriberDetail> = [
        {
          ...requestCache.sender,
          subscriber_url: bap_uri
        }
      ];

      response = await callNetwork(
        subscribers,
        responseBody,
        axios_config,
        action
      );
    }

    if (
      response.status == 200 ||
      response.status == 202 ||
      response.status == 206
    ) {
      // Network Calls Succeeded.
      return;
    }
  } catch (error) {
    logger.error(error);
    let exception: Exception | null = null;
    if (error instanceof Exception) {
      exception = error;
    } else {
      exception = new Exception(
        ExceptionType.Request_Failed,
        "BPP Response Failed at sellerServiceMessageHandler",
        500,
        error
      );
    }
  }
};
