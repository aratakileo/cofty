import src.utils.arataki_typing


def unequal_or_else(src_value, comparable_value, else_value):
    return src_value if src_value != comparable_value else else_value


def as_serial_number(num: int):
    last_digit = num % 10

    if last_digit == 1:
        postfix = 'st'
    elif last_digit == 2:
        postfix = 'nd'
    elif last_digit == 3:
        postfix = 'rd'
    else:
        postfix = 'th'

    return f'{num}{postfix}'
