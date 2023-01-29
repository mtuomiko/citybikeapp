import React, { forwardRef } from "react";
import { Box, Input } from "@chakra-ui/react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const customDateInput = ({ value, onClick, onChange, placeholder }: any, ref: any) => (
  <Input
    size="sm"
    autoComplete="off"
    placeholder={placeholder}
    onClick={onClick}
    value={value}
    ref={ref}
    onChange={onChange}
  />
);

const MonthPicker = ({ date, onChange, placeholder }: {
  date: Date | null
  onChange: React.Dispatch<React.SetStateAction<Date | null>>
  placeholder?: string
}) => {
  const CustomInput = forwardRef(customDateInput);

  return (
    <Box>
      <DatePicker
        showPopperArrow={false}
        selected={date}
        onChange={onChange}
        className="react-datapicker__input-text"
        dateFormat="MMMM yyyy"
        placeholderText={placeholder}
        showMonthYearPicker
        isClearable
        customInput={<CustomInput />}
      />
    </Box>
  );
};

export default MonthPicker;
