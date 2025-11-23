CREATE TABLE historic_candles_series
(
    id        uuid NOT NULL,
    ticker    character varying NOT NULL,
    from_date timestamp without time zone NOT NULL,
    to_date   timestamp without time zone NOT NULL,
    data      jsonb NOT NULL,
    CONSTRAINT historic_candles_series_pkey PRIMARY KEY (id)
);

CREATE TABLE backtesting_task
(
    id         uuid NOT NULL,
    start_date timestamp without time zone NOT NULL,
    end_date   timestamp without time zone,
    status     character varying NOT NULL,
    configuration jsonb NOT NULL,
    CONSTRAINT backtesting_task_pkey PRIMARY KEY (id)
);

CREATE TABLE backtesting_task_historic_candles_series(
    backtesting_task_id uuid NOT NULL,
    historic_candles_series_id uuid NOT NULL,
    PRIMARY KEY (backtesting_task_id, historic_candles_series_id),
    CONSTRAINT backtesting_task_fk FOREIGN KEY (backtesting_task_id)
                    REFERENCES backtesting_task (id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE,
    CONSTRAINT historic_candles_series_fk FOREIGN KEY (historic_candles_series_id)
                    REFERENCES historic_candles_series (id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
);

CREATE TABLE backtesting_task_result
(
    id                  uuid NOT NULL,
    backtesting_task_id uuid NOT NULL,
    total_amount        numeric NOT NULL,
    median_amount       numeric NOT NULL,
    result              jsonb NOT NULL,
    CONSTRAINT backtesting_task_result_pkey PRIMARY KEY (id),
    CONSTRAINT backtesting_task_fk FOREIGN KEY (backtesting_task_id)
                REFERENCES backtesting_task (id)
                ON UPDATE CASCADE
                ON DELETE CASCADE
);