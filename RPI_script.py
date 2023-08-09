#!/usr/bin/env python3

import math
import time
from datetime import datetime
import minimalmodbus
import requests
import pickle
from typing import List
from pytz import utc
import os

instrument = minimalmodbus.Instrument('/dev/ttyUSB0', 1)
instrument.serial.baudrate = 9600
instrument.serial.bytesize = 8
instrument.serial.parity = minimalmodbus.serial.PARITY_EVEN
instrument.serial.timeout = 1
instrument.serial.stopbits = 1
instrument.close_port_after_each_call = True

CURRENT_L1_ADDRESS_16 = "500C"
CURRENT_L2_ADDRESS_16 = "500E"
CURRENT_L3_ADDRESS_16 = "5010"

VOLTAGE_L1_ADDRESS_16 = "5002"
VOLTAGE_L2_ADDRESS_16 = "5004"
VOLTAGE_L3_ADDRESS_16 = "5006"

GRID_FREQUENCY_ADDRESS_16 = "5008"

FUNCTION_CODE = 3
REGISTER_LENGTH = 2
BYTE_ORDER = 0

CURRENT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))
LOG_FILE_PATH = os.path.join(CURRENT_DIRECTORY, "bakalarka_error_log.txt")
UNSENT_FILE_PATH = os.path.join(CURRENT_DIRECTORY, "unsent_data.pkl")

time_delay_between_measurements = 10


def read_float(instrument, register_address_16, function_code, number_of_registers, byte_order) -> float:
    try:
        register_address_10 = int(register_address_16, 16)
        return instrument.read_float(registeraddress=register_address_10, functioncode=function_code, number_of_registers=number_of_registers, byteorder=byte_order)
    except Exception as e:
        error_message = f"Error occurred while reading data from register {register_address_16}: {e}"
        print(error_message)
        log_error(LOG_FILE_PATH, error_message)
        return math.nan


def load_unsent_data(file_name: str) -> dict:
    try:
        with open(file_name, 'rb') as file:
            unsent_data = pickle.load(file)
        return unsent_data
    except Exception as e:
        error_message = f"Error occurred while loading unsent data: {e}"
        print(error_message)
        log_error(LOG_FILE_PATH, error_message)
        return {"currents": [], "gridFrequencies": [], "voltages": []}


def save_unsent_data(file_name: str, unsent_data: dict) -> None:
    try:
        with open(file_name, 'wb') as file:
            pickle.dump(unsent_data, file)
    except Exception as e:
        error_message = f"Error occurred while saving unsent data: {e}"
        print(error_message)
        log_error(LOG_FILE_PATH, error_message)
        return


def send_data_to_rest_controller(payload: dict, url: str, unsent_data_file: str) -> bool:
    combined_payload = load_unsent_data(unsent_data_file)

    for key in payload:
        combined_payload[key].extend(payload[key])

    try:
        response = requests.post(url, json=combined_payload)
        response.raise_for_status()
        save_unsent_data(unsent_data_file, {
                         "currents": [], "gridFrequencies": [], "voltages": []})
        return True
    except requests.exceptions.RequestException as e:
        if isinstance(e, requests.exceptions.ConnectionError):
            error_message = f"Error occurred while connecting to API: {e}"
        else:
            error_message = f"Error occurred sending data to API: {e}"
        print(error_message)
        log_error(LOG_FILE_PATH, error_message)
        save_unsent_data(unsent_data_file, combined_payload)
        return False


def create_electric_quantities_payload(
        currents: List[dict], grid_frequencies: List[dict], voltages: List[dict]) -> dict:
    return {
        "currents": currents,
        "gridFrequencies": grid_frequencies,
        "voltages": voltages,
    }


def filter_nan_values(data_list: List[dict], key: str) -> List[dict]:
    return [x for x in data_list if not math.isnan(x[key])]


def log_error(file_name: str, error_message: str) -> None:
    try:
        with open(file_name, 'a') as file:
            timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            file.write(f"{timestamp} - {error_message}\n\n")
    except Exception as e:
        print(f"Error occurred while logging error: {e}")


def mainLoop(unsent_data_file) -> None:
    try:
        current_L1 = read_float(instrument=instrument, register_address_16=CURRENT_L1_ADDRESS_16,
                                function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

        current_L2 = read_float(instrument=instrument, register_address_16=CURRENT_L2_ADDRESS_16,
                                function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

        current_L3 = read_float(instrument=instrument, register_address_16=CURRENT_L3_ADDRESS_16,
                                function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

        voltage_L1 = read_float(instrument=instrument, register_address_16=VOLTAGE_L1_ADDRESS_16,
                                function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

        voltage_L2 = read_float(instrument=instrument, register_address_16=VOLTAGE_L2_ADDRESS_16,
                                function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

        voltage_L3 = read_float(instrument=instrument, register_address_16=VOLTAGE_L3_ADDRESS_16,
                                function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

        grid_frequency = read_float(instrument=instrument, register_address_16=GRID_FREQUENCY_ADDRESS_16,
                                    function_code=FUNCTION_CODE, number_of_registers=REGISTER_LENGTH, byte_order=BYTE_ORDER)

    except Exception as e:
        error_message = f"Error reading data from electric meter occurred: {e}"
        print(error_message)
        log_error(LOG_FILE_PATH, error_message)

    local_time = datetime.now()
    utc_time = local_time.astimezone(utc)
    time_string = utc_time.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'

    currents = [
        {"current": current_L1, "time": time_string, "phase": "L1"},
        {"current": current_L2, "time": time_string, "phase": "L2"},
        {"current": current_L3, "time": time_string, "phase": "L3"},
    ]

    grid_frequencies = [
        {"frequency": grid_frequency, "time": time_string},
    ]

    voltages = [
        {"voltage": voltage_L1, "time": time_string, "phase": "L1"},
        {"voltage": voltage_L2, "time": time_string, "phase": "L2"},
        {"voltage": voltage_L3, "time": time_string, "phase": "L3"},
    ]

    currents = filter_nan_values(currents, "current")
    grid_frequencies = filter_nan_values(grid_frequencies, "frequency")
    voltages = filter_nan_values(voltages, "voltage")

    electric_quantities_payload = create_electric_quantities_payload(
        currents, grid_frequencies, voltages)

    rest_controller_url = "http://10.10.0.101:8080/api/electric-quantities"
    send_success = send_data_to_rest_controller(
        electric_quantities_payload, rest_controller_url, unsent_data_file)

    if send_success:
        unsent_data = load_unsent_data(unsent_data_file)
        if any(len(data_list) > 0 for data_list in unsent_data.values()):
            unsent_send_success = send_data_to_rest_controller(
                unsent_data, rest_controller_url, unsent_data_file)
            if not unsent_send_success:
                save_unsent_data(unsent_data_file, unsent_data)

if __name__ == "__main__":
    while True:
        mainLoop(UNSENT_FILE_PATH)
        time.sleep(time_delay_between_measurements)
