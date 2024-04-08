import { sellerServiceMessageHandler } from "../controllers/bpp.response.controller";
import { Exception, ExceptionType } from "../models/exception.model";
import { AppMode } from "../schemas/configs/app.config.schema";
import { GatewayMode } from "../schemas/configs/gateway.app.config.schema";
import { getConfig } from "./config.utils";
import { MQClient } from "./rbtmq.utils";

export class GatewayUtils {
    public static getInstance() {
        if (!GatewayUtils.instance) {
            GatewayUtils.instance = new GatewayUtils();
        }
        return GatewayUtils.instance;
    }

    private static instance: GatewayUtils;

    private mqClient: MQClient;

    private constructor() {
        this.mqClient = new MQClient(getConfig().app.gateway.amqpURL);
    }

    public async initialize() {
        await this.mqClient.connect();
        await this.mqClient.assertQueue(getConfig().app.gateway.inboxQueue);
        await this.mqClient.assertQueue(getConfig().app.gateway.outboxQueue);
        switch (getConfig().app.gateway.mode) {

            case GatewayMode.network: {
                switch (getConfig().app.mode) {
                    case AppMode.bpp: {
                        await this.mqClient.consumeMessage(getConfig().app.gateway.outboxQueue, sellerServiceMessageHandler);
                        break;
                    }
                        break;
                }
            }
        }
    }

    public async sendToClientSideGateway(data: any) {
        if (getConfig().app.gateway.mode === GatewayMode.client) {
            throw new Exception(ExceptionType.Gateway_InvalidUse, "Gateway is in client mode, cannot send data to client side gateway", 500);
        }

        await this.mqClient.publishMessage(getConfig().app.gateway.inboxQueue, data);
    }
}