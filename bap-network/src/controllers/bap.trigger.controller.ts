import * as AmqbLib from "amqplib";
import { Exception, ExceptionType } from "../models/exception.model";
import { BecknErrorType } from "../schemas/becknError.schema";
import { BecknResponse } from "../schemas/becknResponse.schema";
import { telemetryCache } from "../schemas/cache/telemetry.cache";
import { RequestActions } from "../schemas/configs/actions.app.config.schema";
import { ClientConfigType } from "../schemas/configs/client.config.schema";
import { NetworkPaticipantType } from "../schemas/subscriberDetails.schema";
import { createAuthHeaderConfig } from "../utils/auth.utils";
import { callNetwork } from "../utils/becknRequester.utils";
import { SyncCache } from "../utils/cache/sync.cache.utils";
import { errorCallback } from "../utils/callback.utils";
import { getConfig } from "../utils/config.utils";
import logger from "../utils/logger.utils";
import { registryLookup } from "../utils/lookup.utils";
import {
  createTelemetryEvent,
  processTelemetry
} from "../utils/telemetry.utils";
const protocolServerLevel = `${getConfig().app.mode.toUpperCase()}-${getConfig().app.gateway.mode.toUpperCase()}`;


export const bapClientMessageConsumer = async (
  message: AmqbLib.ConsumeMessage | null
) => {
  try {
    logger.info(
      "Client Protocol Network Server recieving message from outbox queue"
    );

    const requestBody = JSON.parse(message?.content.toString()!);
    logger.info(`request: ${JSON.stringify(requestBody)}`);

    const context = JSON.parse(JSON.stringify(requestBody.context));
    const axios_config = await createAuthHeaderConfig(requestBody);

    const bpp_id = requestBody.context.bpp_id;
    const bpp_uri = requestBody.context.bpp_uri;
    const action = requestBody.context.action;

    let response: BecknResponse | undefined;
    if (bpp_id && bpp_uri && bpp_id !== "" && bpp_uri !== "") {
      logger.info(
        "Request with subscriber_id, bpp_id provided in request"
      );
      const subscribers = await registryLookup({
        type: NetworkPaticipantType.BPP,
        domain: requestBody.context.domain,
        subscriber_id: bpp_id
      });

      for (let i = 0; i < subscribers!.length; i++) {
        subscribers![i].subscriber_url = bpp_uri;
      }

      response = await callNetwork(
        subscribers!,
        requestBody,
        axios_config,
        action
      );
    } else {
      const subscribers = await registryLookup({
        type: NetworkPaticipantType.BG,
        domain: requestBody.context.domain
      });

      response = await callNetwork(
        subscribers!,
        requestBody,
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
      // Generate Telemetry if enabled
      if (getConfig().app.telemetry.enabled && getConfig().app.telemetry.url) {
        telemetryCache.get("bap_client_settled")?.push(
          createTelemetryEvent({
            context: requestBody.context,
            data: response
          })
        );
        await processTelemetry();
      }
      return;
    }

    switch (getConfig().client.type) {
      case ClientConfigType.synchronous: {
        const message_id = requestBody.context.message_id;
        await SyncCache.getInstance().recordError(
          message_id,
          action as RequestActions,
          {
            // TODO: change this error code.
            code: 651641,
            type: BecknErrorType.coreError,
            message: "Network Participant Request Failed...",
            data: [response]
          }
        );
        break;
      }
      case ClientConfigType.messageQueue: {
        // TODO: Implement message queue.
        break;
      }
      case ClientConfigType.webhook: {
        await errorCallback(context, {
          // TODO: change this error code.
          code: 651641,
          type: BecknErrorType.coreError,
          message: "Network Participant Request Failed...",
          data: [response]
        });
        break;
      }
    }

    return;
  } catch (err) {
    let exception: Exception | null = null;
    if (err instanceof Exception) {
      exception = err;
    } else {
      exception = new Exception(
        ExceptionType.Request_Failed,
        "BAP Request Failed at bapClientMessageConsumer",
        500,
        err
      );
    }

    logger.error(exception);
  }
};
