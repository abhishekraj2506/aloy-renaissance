import * as AmqbLib from "amqplib";
import { Exception, ExceptionType } from "../models/exception.model";
import { telemetryCache } from "../schemas/cache/telemetry.cache";
import { ClientConfigType } from "../schemas/configs/client.config.schema";
import { ActionUtils } from "../utils/actions.utils";
import { SyncCache } from "../utils/cache/sync.cache.utils";
import { responseCallback } from "../utils/callback.utils";
import { getConfig } from "../utils/config.utils";
import logger from "../utils/logger.utils";
import {
  createTelemetryEvent,
  processTelemetry,
} from "../utils/telemetry.utils";


export const clientProtocolResponseConsumer = async (
  message: AmqbLib.ConsumeMessage | null
) => {
  try {
    logger.info(
      "Client Server recieving message from client protocol service in inbox queue"
    );

    const responseBody = JSON.parse(message?.content.toString()!);

    logger.info(
      `Actual Response from Seller NETWORK:\n ${JSON.stringify(responseBody)}\n\n`
    );

    const message_id = responseBody.context.message_id;
    const action = ActionUtils.getCorrespondingRequestAction(
      responseBody.context.action
    );
    // Generate telemetry if enabled
    if (getConfig().app.telemetry.enabled && getConfig().app.telemetry.url) {
      telemetryCache
        .get("bap_response_settled")
        ?.push(createTelemetryEvent({ context: responseBody.context }));
      await processTelemetry();
    }
    switch (getConfig().client.type) {
      case ClientConfigType.synchronous: {
        await SyncCache.getInstance().insertResponse(
          message_id,
          action,
          responseBody
        );
        break;
      }
      case ClientConfigType.webhook: {
        responseCallback(responseBody);
        break;
      }
      case ClientConfigType.messageQueue: {
        // TODO: implement message queue
        break;
      }
    }
  } catch (err) {
    let exception: Exception | null = null;
    if (err instanceof Exception) {
      exception = err;
    } else {
      exception = new Exception(
        ExceptionType.Response_Failed,
        "BAP Response Failed at clientProtocolResponseConsumer",
        500,
        err
      );
    }

    logger.error(err);
  }
};
