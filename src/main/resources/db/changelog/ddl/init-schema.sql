CREATE TABLE strategy_configuration
(
    id              uuid              NOT NULL,
    name            character varying NOT NULL,
    type            character varying NOT NULL,
    candle_interval character varying NOT NULL,
    parameters      jsonb             NOT NULL,
    CONSTRAINT strategy_configuration_pkey PRIMARY KEY (id)
);

CREATE TABLE trade_session
(
    id                        uuid              NOT NULL,
    ticker                    character varying NOT NULL,
    instrument_id             character varying NOT NULL,
    status                    character varying NOT NULL,
    candle_interval           character varying NOT NULL,
    lots_quantity             integer           NOT NULL,
    lots_quantity_in_position integer           NOT NULL,
    strategy_type             character varying NOT NULL,
    strategy_parameters       jsonb             NOT NULL,
    CONSTRAINT trade_session_pkey PRIMARY KEY (id)
);

CREATE TABLE trade_order
(
    id                  uuid              NOT NULL,
    ticker              character varying NOT NULL,
    instrument_id       character varying NOT NULL,
    date                timestamp without time zone NOT NULL,
    lots_quantity       integer           NOT NULL,
    total_price         numeric           NOT NULL,
    executed_commission numeric           NOT NULL,
    direction           character varying NOT NULL,
    trade_session_id    uuid              NOT NULL,
    CONSTRAINT trade_order_pkey PRIMARY KEY (id),
    CONSTRAINT trade_session_fk FOREIGN KEY (trade_session_id)
            REFERENCES trade_session (id) MATCH SIMPLE
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

CREATE TABLE instrument
(
    id            uuid              NOT NULL,
    ticker        character varying NOT NULL,
    instrument_id character varying NOT NULL,
    CONSTRAINT instrument_pkey PRIMARY KEY (id)
)