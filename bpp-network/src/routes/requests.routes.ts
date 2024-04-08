import { NextFunction, Request, Response, Router } from "express";
import { sellerProtocolApiRequestHandler } from "../controllers/bpp.request.controller";
import { unConfigureActionHandler } from "../controllers/unconfigured.controller";
import { Locals } from "../interfaces/locals.interface";
import {
  authValidatorMiddleware
} from "../middlewares/auth.middleware";
import { jsonCompressorMiddleware } from "../middlewares/jsonParser.middleware";
import { openApiValidatorMiddleware } from "../middlewares/schemaValidator.middleware";
import {
  RequestActions
} from "../schemas/configs/actions.app.config.schema";
import { AppMode } from "../schemas/configs/app.config.schema";
import { GatewayMode } from "../schemas/configs/gateway.app.config.schema";
import { getConfig } from "../utils/config.utils";
import logger from "../utils/logger.utils";
export const requestsRouter = Router();

// BPP Network-Side Gateway Configuration.
if (
  getConfig().app.mode == AppMode.bpp &&
  getConfig().app.gateway.mode === GatewayMode.network
) {
  const requestActions = getConfig().app.actions.requests;
  logger.info(`request actions ${requestActions}`)
  Object.keys(RequestActions).forEach((action) => {
    if (requestActions[action as RequestActions]) {
      requestsRouter.post(
        `/${action}`,
        jsonCompressorMiddleware,
        authValidatorMiddleware,
        openApiValidatorMiddleware,
        async (req: Request, res: Response<{}, Locals>, next: NextFunction) => {
          await sellerProtocolApiRequestHandler(
            req,
            res,
            next,
            action as RequestActions
          );
        }
      );
    } else {
      requestsRouter.post(
        `/${action}`,
        async (req: Request, res: Response, next: NextFunction) => {
          await unConfigureActionHandler(req, res, next, action);
        }
      );
    }
  });
}
