package com.mtuomiko.citybikeapp.api.mapper

import com.mtuomiko.citybikeapp.api.model.APIStationDetails
import com.mtuomiko.citybikeapp.api.model.APIStationStatistics
import com.mtuomiko.citybikeapp.api.model.APITopStation
import com.mtuomiko.citybikeapp.common.model.StationDetails
import com.mtuomiko.citybikeapp.common.model.TopStation
import com.mtuomiko.citybikeapp.svc.model.StationStatistics
import org.mapstruct.Mapper

@Mapper(componentModel = "jsr330")
interface StationAPIMapper {

    fun toApi(stationDetails: StationDetails): APIStationDetails

    fun toApi(stationStatistics: StationStatistics): APIStationStatistics

    fun toApi(topStation: TopStation): APITopStation
}
