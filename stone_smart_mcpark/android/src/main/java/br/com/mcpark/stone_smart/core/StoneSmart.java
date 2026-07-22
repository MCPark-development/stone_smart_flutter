package br.com.mcpark.stone_smart.core;
import android.content.Context;
import android.util.Log;
import br.com.mcpark.stone_smart.payments.PaymentsPresenter;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;


public class StoneSmart {
    final MethodChannel mChannel;

    PaymentsPresenter payment;

    final Context currentContext;
    private String TAG = "StoneSmart";
    private boolean isDebugLogActive = false;


    public StoneSmart(Context context, MethodChannel channel) {
        this.currentContext = context;
        this.mChannel = channel;
    }

    public void initPayment(MethodCall call, MethodChannel.Result result) {
        if (this.payment == null) {
            this.payment = new PaymentsPresenter(this.mChannel, isDebugLogActive);
        }

        Log.i(TAG, "Call Method: " + call.method);
        PaymentMethod paymentMethod = PaymentMethod.fromString(call.method);
        if (paymentMethod == null) {
            result.notImplemented();
            return;
        }

        switch (paymentMethod) {
            case PAYMENT_ACTIVE_DEBUG_LOG:
                boolean isDebugLogParams = call.argument("isDebugLog");
                isDebugLogActive = isDebugLogParams;
                this.payment = new PaymentsPresenter(this.mChannel, isDebugLogActive);
                break;
            // Consultas sincronas: respondem o proprio valor e encerram aqui.
            case PAYMENT_GET_SERIAL_NUMBER:
                String serialNumber = this.payment.getPosSerialNumber();
                result.success(serialNumber);
                return;
            case PAYMENT_GET_MANUFACTURE:
                String posManufacture = this.payment.getPosManufacture();
                result.success(posManufacture);
                return;
            case PAYMENT_CUSTOM_PRINTER:
                String printerParams = call.argument("printerParams");
                this.payment.customPrinter(printerParams, currentContext);
                break;

            case PAYMENT_PRINT_BASE64:
                String base64PrinterParams = call.argument("printerParams");
                this.payment.printFromBase64(base64PrinterParams, currentContext);
                break;

            case PAYMENT_PRINT_WRAP_PAPER:
                int lines = call.argument("lines");
                this.payment.printWrapPaper(lines, currentContext);
                break;

            case PAYMENT_GET_ALL_TRANSACTIONS:
                this.payment.getAllTransactions(currentContext);
                break;

            case PAYMENT_REVERSAL:
                this.payment.onReversal(currentContext);
                break;

            case PAYMENT_GET_TRANSACTION_BY_INITIATOR_TRANSACTION_KEY:
                String initiatorTransactionKey = call.argument("InitiatorTransactionKey");
                this.payment.getTransactionByInitiatorTransactionKey(currentContext, initiatorTransactionKey);
                break;

            case PAYMENT_PRINTER_TRANSACTION:
                boolean printCustomerSlip = call.argument("printCustomerSlip");
                this.payment.printerCurrentTransaction(currentContext, printCustomerSlip);
                break;

            case PAYMENT_PRINTER_TRANSACTION_KEY:
                String transactionKey = call.argument("paymentPrinterTransactionKey");
                this.payment.printerFromTransactionKey(currentContext, transactionKey);
                break;

            case ACTIVE_PINPAD:
                String appName = call.argument("appName");
                String stoneCode = call.argument("stoneCode");
                this.payment.activate(appName, stoneCode, currentContext);
                break;

            case ACTIVE_PINPAD_CREDENTIALS:
                this.payment.activateWithCredentials(
                    call.argument("appName"),
                    call.argument("stoneCode"),
                    call.argument("qrCodeAuthorization"),
                    call.argument("qrCodeProviderid"),
                    currentContext
                );
                break;

            case PAYMENT_ABORT_PIX:
                this.payment.abortPIXtransaction(currentContext);
                break;

            case PAYMENT_ABORT:
                this.payment.abortCurrentPosTransaction();
                break;

            case PAYMENT_OPTIONS:
                this.payment.setPaymentOption(call.argument("option"));
                break;

            case PAYMENT_CANCEL_TRANSACTION:
                int idFromBase = call.argument("idFromBase");
                this.payment.cancelTransaction(currentContext, idFromBase);
                break;

            default:
                String amount = call.argument("amount");
                int parc = call.argument("installment");
                boolean withInterest = call.argument("withInterest");
                boolean withCustomerSlip = call.argument("printCustomerSlip");
                boolean withEstablishmentSlip = call.argument("printEstablishmentSlip");
                String initiatorKey = call.argument("initiatorTransactionKey");

                switch (paymentMethod) {
                    case PAYMENT_DEBIT:
                        this.payment.doTransaction(currentContext, amount, 2, initiatorKey, parc, withInterest, null, null, withCustomerSlip, withEstablishmentSlip);
                        break;

                    case PAYMENT_PIX:
                        String withQrCodeAuthorization = call.argument("qrCodeAuthorization");
                        String withQrCodeProviderid = call.argument("qrCodeProviderid");
                        this.payment.doTransaction(currentContext, amount, 3, initiatorKey, parc, withInterest,
                                withQrCodeAuthorization, withQrCodeProviderid, withCustomerSlip, withEstablishmentSlip);
                        break;

                    case PAYMENT_CREDIT:
                    case PAYMENT_CREDIT_PARC:
                        this.payment.doTransaction(currentContext, amount, 1, initiatorKey, parc, withInterest, null, null, withCustomerSlip, withEstablishmentSlip);
                        break;

                    case PAYMENT_VOUCHER:
                        this.payment.doTransaction(currentContext, amount, 4, initiatorKey, parc, withInterest, null, null, withCustomerSlip, withEstablishmentSlip);
                        break;

                    default:
                        result.notImplemented();
                        return;
                }
                break;
        }

        // ─────────────────────────────────────────────────────────────────────
        // CORRECAO MCPARK — o MethodChannel PRECISA ser respondido.
        //
        // Antes, todos os comandos "fire-and-forget" (ativacao do pinpad,
        // pagamentos, abort, abortPIX, cancelTransaction, impressao...) caiam
        // num `break;` que NUNCA tocava em `result`. Do lado Dart,
        // `await channel.invokeMethod(...)` entao JAMAIS completava: a
        // ativacao pendurava o app para sempre, sem timeout e sem saida, e o
        // cancelamento travava a UI em "Cancelando...".
        //
        // Este `success(true)` confirma apenas o DESPACHO do comando. O
        // desfecho real continua chegando pelos callbacks do handler
        // (onFinishedResponse / onError / onAbortedSuccessfully) — a
        // arquitetura orientada a evento nao muda. As duas consultas sincronas
        // acima respondem o proprio valor e retornam antes daqui.
        //
        // `true` (e nao `null`) porque o lado Dart tipa a maioria destas
        // chamadas como `Future<bool>` com `return await invokeMethod(...)`:
        // um `null` estouraria erro de cast.
        result.success(true);
    }

    public void dispose() {
        if (this.payment != null) {
            this.payment.dispose();
        }
    }
}
