import { NextFunction, Request, Response, Router } from "express";
import { clientApiRequestHandler } from "../controllers/bap.trigger.controller";
import { unConfigureActionHandler } from "../controllers/unconfigured.controller";
import { Locals } from "../interfaces/locals.interface";
import {
  authBuilderMiddleware
} from "../middlewares/auth.middleware";
import { contextBuilderMiddleware } from "../middlewares/context.middleware";
import { jsonCompressorMiddleware } from "../middlewares/jsonParser.middleware";
import { openApiValidatorMiddleware } from "../middlewares/schemaValidator.middleware";
import {
  RequestActions
} from "../schemas/configs/actions.app.config.schema";
import { AppMode } from "../schemas/configs/app.config.schema";
import { GatewayMode } from "../schemas/configs/gateway.app.config.schema";
import { getConfig } from "../utils/config.utils";
export const requestsRouter = Router();

// BAP Client-Side Gateway Configuration.
if (
  getConfig().app.mode === AppMode.bap &&
  getConfig().app.gateway.mode === GatewayMode.client
) {
  const requestActions = getConfig().app.actions.requests;
  Object.keys(RequestActions).forEach((action) => {
    if (requestActions[action as RequestActions]) {
      requestsRouter.post(
        `/${action}`,
        jsonCompressorMiddleware,
        async (req: Request, res: Response<{}, Locals>, next: NextFunction) => {
          await contextBuilderMiddleware(req, res, next, action);
        },
        authBuilderMiddleware,
        openApiValidatorMiddleware,
        async (req: Request, res: Response<{}, Locals>, next: NextFunction) => {
          await clientApiRequestHandler(
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