import cats.effect.kernel.Async
import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import dolphin.StoreSession
import dolphin.circe.domain.ReadDecodeResult.{Failure, Success}
import dolphin.option.{ReadOptions, WriteOptions}
import fs2.Stream
import io.circe.generic.semiauto.*
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  // Domain
  final case class ShoppingCartId(value: String) extends AnyVal

  object ShoppingCartId {
    implicit val shoppingCartIdEncoder: Encoder[ShoppingCartId] = deriveEncoder
    implicit val shoppingCartIdDecoder: Decoder[ShoppingCartId] = deriveDecoder
  }

  final case class ItemId(value: String) extends AnyVal

  object ItemId {
    implicit val itemIdEncoder: Encoder[ItemId] = deriveEncoder
    implicit val itemIdDecoder: Decoder[ItemId] = deriveDecoder
  }

  final case class Quantity(value: Int) extends AnyVal

  object Quantity {
    implicit val quantityEncoder: Encoder[Quantity] = deriveEncoder
    implicit val quantityDecoder: Decoder[Quantity] = deriveDecoder
  }

  final case class Item(id: ItemId, quantity: Quantity)

  object Item {
    implicit val itemEncoder: Encoder[Item] = deriveEncoder
    implicit val itemDecoder: Decoder[Item] = deriveDecoder
  }

  // State
  final case class ShoppingCart(shoppingCartId: ShoppingCartId, items: List[Item]) { self =>
    def isEmpty: Boolean = items.isEmpty

    def contains(id: ItemId): Boolean = items.exists(_.id == id)

    def quantityOf(id: ItemId): Option[Quantity] = items.find(_.id == id).map(_.quantity)

    def updateQuantity(id: ItemId, quantity: Quantity): ShoppingCart = copy(items =
      items.map(item =>
        if (item.id == id)
          item.copy(quantity = quantity)
        else
          item
      )
    )

    def removeItem(id: ItemId): ShoppingCart = copy(items = items.filterNot(_.id == id))

    /// if item is already in the cart, update its quantity
    def addItem(item: Item): ShoppingCart =
      if (contains(item.id)) {
        quantityOf(item.id) match {
          case Some(quantity) => updateQuantity(item.id, Quantity(quantity.value + item.quantity.value))
          case None           => self
        }

      } else {
        copy(items = item :: items)
      }

  }

  object ShoppingCart {
    def empty(shoppingCartId: ShoppingCartId): ShoppingCart = ShoppingCart(shoppingCartId, List.empty)

    def when(entity: ShoppingCart, event: Event): ShoppingCart =
      event match {
        case ShoppingCartCreated(id)              => ShoppingCart.empty(id)
        case ItemAdded(_, id, quantity)           => entity.addItem(Item(id, quantity))
        case ItemRemoved(_, id)                   => entity.removeItem(id)
        case ItemQuantityChanged(_, id, quantity) => entity.updateQuantity(id, quantity)
        case ShoppingCartCheckedOut(_)            => entity
      }

  }

  // Build a shopping cart EventSourced application

  // Commands
  // - AddItem
  // - RemoveItem
  // - ChangeItemQuantity
  // - Checkout

  sealed trait Command
  final case class CreateShoppingCart(shoppingCartId: ShoppingCartId)                          extends Command
  final case class AddItem(shoppingCartId: ShoppingCartId, itemId: ItemId, quantity: Quantity) extends Command
  final case class RemoveItem(shoppingCartId: ShoppingCartId, itemId: ItemId)                  extends Command
  final case class ChangeItemQuantity(shoppingCartId: ShoppingCartId, itemId: ItemId, quantity: Quantity)
    extends Command
  final case class Checkout(shoppingCartId: ShoppingCartId) extends Command

  // Events
  // - ShoppingCartCreated
  // - ItemAdded
  // - ItemRemoved
  // - ItemQuantityChanged
  // - ShoppingCartCheckedOut

  sealed trait Event { self =>

    def handle(command: Command): Event =
      command match {
        case CreateShoppingCart(shoppingCartId)        => ShoppingCartCreated(shoppingCartId)
        case AddItem(shoppingCartId, itemId, quantity) => ItemAdded(shoppingCartId, itemId, quantity)
        case RemoveItem(shoppingCartId, itemId)        => ItemRemoved(shoppingCartId, itemId)
        case ChangeItemQuantity(shoppingCartId, itemId, quantity) =>
          ItemQuantityChanged(shoppingCartId, itemId, quantity)
        case Checkout(shoppingCartId) => ShoppingCartCheckedOut(shoppingCartId)
      }

  }

  object Event {

    implicit val eventEncoder: Encoder[Event] = {
      case c: ShoppingCartCreated    => ShoppingCartCreated.encoder(c)
      case i: ItemAdded              => ItemAdded.encoder(i)
      case i: ItemRemoved            => ItemRemoved.encoder(i)
      case i: ItemQuantityChanged    => ItemQuantityChanged.encoder(i)
      case s: ShoppingCartCheckedOut => ShoppingCartCheckedOut.encoder(s)

    }

    implicit val eventDecoder: Decoder[Event] = ShoppingCartCreated
      .decoder
      .widen[Event]
      .or(ItemAdded.decoder.widen[Event])
      .or(ItemRemoved.decoder.widen[Event])
      .or(ItemQuantityChanged.decoder.widen[Event])
      .or(ShoppingCartCheckedOut.decoder.widen[Event])

  }

  final case class ShoppingCartCreated(shoppingCartId: ShoppingCartId) extends Event

  object ShoppingCartCreated {
    implicit val encoder = deriveEncoder[ShoppingCartCreated]
    implicit val decoder = deriveDecoder[ShoppingCartCreated]
  }

  final case class ItemAdded(shoppingCartId: ShoppingCartId, itemId: ItemId, quantity: Quantity) extends Event

  object ItemAdded {
    implicit val encoder = deriveEncoder[ItemAdded]
    implicit val decoder = deriveDecoder[ItemAdded]
  }

  final case class ItemRemoved(shoppingCartId: ShoppingCartId, itemId: ItemId) extends Event

  object ItemRemoved {
    implicit val encoder = deriveEncoder[ItemRemoved]
    implicit val decoder = deriveDecoder[ItemRemoved]
  }

  final case class ItemQuantityChanged(shoppingCartId: ShoppingCartId, itemId: ItemId, quantity: Quantity) extends Event

  object ItemQuantityChanged {
    implicit val encoder = deriveEncoder[ItemQuantityChanged]
    implicit val decoder = deriveDecoder[ItemQuantityChanged]
  }

  final case class ShoppingCartCheckedOut(shoppingCartId: ShoppingCartId) extends Event

  object ShoppingCartCheckedOut {
    implicit val encoder = deriveEncoder[ShoppingCartCheckedOut]
    implicit val decoder = deriveDecoder[ShoppingCartCheckedOut]
  }

  // Example
  val shoppingCartId = ShoppingCartId("shopping-cart-id")
  val itemId         = ItemId("item-id")
  val quantity       = Quantity(1)

  val shoeId   = ItemId("shoe-id")
  val shoe     = Item(shoeId, Quantity(1))
  val tshirtId = ItemId("tshirt-id")
  val tshirt   = Item(tshirtId, Quantity(1))

  val events: List[Event] = List(
    ShoppingCartCreated(shoppingCartId),
    ItemAdded(shoppingCartId, itemId, quantity),
    ItemAdded(shoppingCartId, shoeId, shoe.quantity),
    ItemAdded(shoppingCartId, tshirtId, tshirt.quantity),
    ItemRemoved(shoppingCartId, itemId),
    ItemQuantityChanged(shoppingCartId, shoeId, Quantity(2)),
    ShoppingCartCheckedOut(shoppingCartId),
  )

  // Aggregate
  private val shoppingCart: ShoppingCart = events.foldLeft(ShoppingCart.empty(shoppingCartId))(ShoppingCart.when)

  // Usually in event sourced applications, the state is persisted in a database and we have a 1:1 mapping between the commands and the events.

  trait EventStore[F[_], TEntity, TEvent] {
    def get(id: String, when: TEntity => TEvent): F[Option[TEntity]]
    def append(id: String, event: TEvent, eventType: String): F[Unit]
    def append(id: String, event: TEvent, eventType: String, version: Long): F[Unit]
  }

  object EventStore {
    import cats.syntax.all.*
    import dolphin.circe.syntax.reader.*

    def make[F[_]: Async](session: StoreSession[F]): EventStore[F, ShoppingCart, Event] =
      new EventStore[F, ShoppingCart, Event] {

        override def get(
          id: String,
          when: ShoppingCart => Event,
        ): F[Option[ShoppingCart]] = session.read(id, ReadOptions.default).flatMap { result =>
          result
            .decodeAs[Event]
            .flatMap {
              case Failure(_)     => Stream(none[ShoppingCart])
              case Success(value) => Stream(Some(ShoppingCart.when(ShoppingCart.empty(ShoppingCartId(id)), value)))
            }
            .compile
            .lastOrError

        }

        override def append(
          id: String,
          event: Event,
          eventType: String,
        ): F[Unit] = session.write(id, event.asJson.noSpaces.getBytes, eventType).as(())

        override def append(id: String, event: Event, eventType: String, version: Long): F[Unit] = session
          .write(id, WriteOptions.default.withExpectedRevision(version), event.asJson.noSpaces.getBytes, eventType)
          .as(())

      }

  }

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] = StoreSession
    .resource[IO](
      host = "localhost",
      port = 2113,
      tls = false,
    )
    .map(client => EventStore.make[IO](client))
    .use(session => session.append(shoppingCartId.value, events.head, "ShoppingCartCreated"))

}
