import { NextFunction, Request, Response } from "express";
import { Locals } from "../interfaces/locals.interface";
import { Exception, ExceptionType } from "../models/exception.model";
import { BecknErrorType } from "../schemas/becknError.schema";
import { parseRequestCache } from "../schemas/cache/request.cache.schema";
import { RequestActions } from "../schemas/configs/actions.app.config.schema";
import {
  acknowledgeACK,
  acknowledgeNACK
} from "../utils/acknowledgement.utils";
import { RequestCache } from "../utils/cache/request.cache.utils";
import { getConfig } from "../utils/config.utils";
import { GatewayUtils } from "../utils/gateway.utils";
import logger from "../utils/logger.utils";
const protocolServerLevel = `${getConfig().app.mode.toUpperCase()}-${getConfig().app.gateway.mode.toUpperCase()}`;

export const clientApiRequestHandler = async (
  req: Request,
  res: Response<{}, Locals>,
  next: NextFunction,
  action: RequestActions
) => {
  try {
    const bpp_id: string | undefined = req.body.context.bpp_id;
    const bpp_uri: string | undefined = req.body.context.bpp_uri;
    if (
      action != RequestActions.search &&
      (!bpp_id || !bpp_uri || bpp_id == "" || bpp_uri == "")
    ) {
      acknowledgeNACK(res, req.body.context, {
        // TODO: change the error code.
        code: 6781616,
        message: `All triggers other than search requires bpp_id and bpp_uri. \nMissing bpp_id or bpp_uri at ${protocolServerLevel}`,
        type: BecknErrorType.contextError
      });
      return;
    }

    acknowledgeACK(res, req.body.context);
    // if (getConfig().client.type == ClientConfigType.webhook) {
    //   acknowledgeACK(res, req.body.context);
    // }
    await RequestCache.getInstance().cache(
      parseRequestCache(
        req.body.context.transaction_id,
        req.body.context.message_id,
        action,
        res.locals.sender!
      ),
      getConfig().app.actions.requests[action]?.ttl!
    );

    logger.info(
      `Client API request received, sending message to outbox queue at ${protocolServerLevel}\n\n`
    );
    logger.info(`Message sent to queue:\n ${JSON.stringify(req.body)}\n`);
    await GatewayUtils.getInstance().sendToNetworkSideGateway(req.body);

    // if (getConfig().client.type == ClientConfigType.synchronous) {
    //   sendSyncResponses(
    //     res,
    //     req.body.context.message_id,
    //     action,
    //     req.body.context
    //   );
    // }
  } catch (err) {
    console.log("Error Occured at clientApiRequestHandler: ", err);
    let exception: Exception | null = null;
    if (err instanceof Exception) {
      exception = err;
    } else {
      exception = new Exception(
        ExceptionType.Request_Failed,
        "BAP Request Failed at clientApiRequestHandler",
        500,
        err
      );
    }

    logger.error(exception);
  }
};