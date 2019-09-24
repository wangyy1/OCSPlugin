class LocationInfo {
  /// 维度
  double latitude;

  /// 经度
  double longitude;

  /// 当前地址
  String address;

  LocationInfo.map(Map map){
    latitude = double.parse(map['latitude']);
    longitude = double.parse(map['longitude']);
    address = map['address'];
  }
}
