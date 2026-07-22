import 'package:stone_smart_mcpark/stone_smart_mcpark.dart';

extension PrinterTypeExt on PrinterType {
  String get type {
    const Map<PrinterType, String> printerTypeMap = {
      PrinterType.text: "text",
      PrinterType.image: "base64",
    };
    return printerTypeMap[this] ?? "text";
  }
}