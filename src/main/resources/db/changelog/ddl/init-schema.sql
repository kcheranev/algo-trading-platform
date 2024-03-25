CREATE TABLE strategy_configuration
(
    id                 uuid              NOT NULL DEFAULT gen_random_uuid(),
    type               character varying NOT NULL,
    init_candle_amount integer           NOT NULL,
    candle_interval    character varying NOT NULL,
    params             jsonb             NOT NULL,
    CONSTRAINT strategy_configuration_pkey PRIMARY KEY (id)
);

CREATE TABLE trade_order
(
    id                        uuid              NOT NULL DEFAULT gen_random_uuid(),
    ticker                    character varying NOT NULL,
    instrument_id             character varying NOT NULL,
    date                      timestamp without time zone NOT NULL,
    lots_quantity             integer           NOT NULL,
    total_price               numeric           NOT NULL,
    executed_commission       numeric           NOT NULL,
    direction                 character varying NOT NULL,
    strategy_configuration_id uuid              NOT NULL,
    CONSTRAINT trade_order_pkey PRIMARY KEY (id),
    CONSTRAINT strategy_configuration_fk FOREIGN KEY (strategy_configuration_id)
        REFERENCES strategy_configuration (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);